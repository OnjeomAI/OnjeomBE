package com.onjeom.backend.domain.ai;

import com.onjeom.backend.domain.ai.dto.*;
import com.onjeom.backend.domain.ai.service.AiGradingResult;
import com.onjeom.backend.domain.ai.service.AiKeyword;
import com.onjeom.backend.domain.ai.service.AiScoringServiceWritingImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiScoringServiceWritingImpl — AI 채점 응답 매핑 및 폴백 테스트")
class AiScoringServiceWritingImplTest {

    @Mock RestClient aiRestClient;
    @Mock RestClient.RequestBodyUriSpec uriSpec;
    @Mock RestClient.RequestBodySpec bodySpec;
    @Mock RestClient.ResponseSpec responseSpec;

    AiScoringServiceWritingImpl service;

    @BeforeEach
    void setUp() {
        service = new AiScoringServiceWritingImpl(aiRestClient);
        given(aiRestClient.post()).willReturn(uriSpec);
        given(uriSpec.uri(anyString())).willReturn(bodySpec);
        given(bodySpec.body(any())).willReturn(bodySpec);
        given(bodySpec.retrieve()).willReturn(responseSpec);
    }

    // ── 정상 응답 매핑 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("정상 응답: finalScore·feedback·errorTypes 올바르게 매핑")
    void scoreAnswer_validResponse_mapsCorrectly() {
        var aiResponse = new WritingEvaluateResponse(
                80, 3, 75, 85,
                "핵심을 잘 파악했습니다.", "EXCELLENT", "심화 학습을 추천합니다.",
                List.of("자연보호"), List.of("생태계"), null
        );
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(aiResponse);

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범답안", List.of(), "답변", "FACTUAL"
        );

        assertThat(result.score()).isEqualTo(85);
        assertThat(result.feedback()).isEqualTo("핵심을 잘 파악했습니다.");
        assertThat(result.foundKeywords()).containsExactly("자연보호");
        assertThat(result.errorTypes()).isEmpty();
    }

    @Test
    @DisplayName("keyword_score 있으면 stage1Score로 사용")
    void scoreAnswer_withKeywordScore_usesAsStage1Score() {
        var aiResponse = new WritingEvaluateResponse(
                80, 3, 75, 85,
                "피드백", "EXCELLENT", "안내",
                List.of(), List.of(), null
        );
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(aiResponse);

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범", List.of(), "답변", "FACTUAL"
        );

        assertThat(result.stage1Score()).isEqualTo(80);
    }

    @Test
    @DisplayName("keyword_score null이면 finalScore를 stage1Score로 사용")
    void scoreAnswer_nullKeywordScore_usesFinalScoreAsStage1() {
        var aiResponse = new WritingEvaluateResponse(
                null, 3, 75, 75,
                "피드백", "GOOD", "안내",
                List.of(), List.of(), null
        );
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(aiResponse);

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범", List.of(), "답변", "FACTUAL"
        );

        assertThat(result.stage1Score()).isEqualTo(75);
    }

    @Test
    @DisplayName("deep_analysis 있으면 errorTypes 추출")
    void scoreAnswer_withDeepAnalysis_extractsErrorTypes() {
        var deepAnalysis = new DeepAnalysisDto(
                List.of("개념 혼동", "내용 누락"), "분석 내용", "개선 방향"
        );
        var aiResponse = new WritingEvaluateResponse(
                null, 1, 25, 25,
                "피드백", "NEEDS_IMPROVEMENT", "안내",
                List.of(), List.of("키워드"), deepAnalysis
        );
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(aiResponse);

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범", List.of(), "답변", "INFERENTIAL"
        );

        assertThat(result.errorTypes()).containsExactlyInAnyOrder("개념 혼동", "내용 누락");
    }

    // ── 폴백 처리 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AI 서버 응답 null → 폴백 score=50 반환")
    void scoreAnswer_nullResponse_returnsFallback() {
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(null);

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범", List.of(), "답변", "FACTUAL"
        );

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.errorTypes()).isEmpty();
    }

    @Test
    @DisplayName("RestClientException → 폴백 score=50 반환")
    void scoreAnswer_restClientException_returnsFallback() {
        given(bodySpec.retrieve()).willThrow(new RestClientException("연결 실패"));

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범", List.of(), "답변", "CRITICAL"
        );

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.feedback()).contains("실패");
    }

    @Test
    @DisplayName("키워드가 있으면 KeywordDto로 변환해서 요청 전달")
    void scoreAnswer_withKeywords_convertsToKeywordDto() {
        given(responseSpec.body(WritingEvaluateResponse.class)).willReturn(
                new WritingEvaluateResponse(null, 3, 75, 75, "피드백", "GOOD",
                        "안내", List.of(), List.of(), null)
        );

        service.scoreAnswer("지문", "문제", "모범",
                List.of(new AiKeyword("자연보호", 60), new AiKeyword("생태계", 40)),
                "답변", "FACTUAL");

        verify(bodySpec).body(argThat((WritingEvaluateRequest req) ->
                req.keywords() != null && req.keywords().size() == 2
        ));
    }

    // ── helper ───────────────────────────────────────────────────────────────
    private static <T> org.mockito.ArgumentMatcher<T> argThat(
            java.util.function.Predicate<T> p) {
        return p::test;
    }
}
