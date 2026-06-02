package com.onjeom.backend.domain.ai;

import com.onjeom.backend.domain.ai.service.AiGradingResult;
import com.onjeom.backend.domain.ai.service.AiScoringServiceWritingImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiScoringServiceWritingImpl — 폴백 처리 테스트")
class AiScoringServiceWritingImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RestClient aiRestClient;

    AiScoringServiceWritingImpl service;

    @BeforeEach
    void setUp() {
        service = new AiScoringServiceWritingImpl(aiRestClient);
    }

    @Test
    @DisplayName("AI 서버 응답 null → score=50 폴백, errorTypes 빈 리스트")
    void scoreAnswer_nullResponse_returnsFallback() {
        // RETURNS_DEEP_STUBS가 WritingEvaluateResponse를 null로 반환 → 폴백 경로 진입
        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범답안", List.of(), "학생 답변", "FACTUAL");

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.stage1Score()).isEqualTo(50);
        assertThat(result.errorTypes()).isEmpty();
        assertThat(result.feedback()).isNotBlank();
    }

    @Test
    @DisplayName("RestClientException → score=50 폴백, 연결 실패 메시지")
    void scoreAnswer_restClientException_returnsFallback() {
        given(aiRestClient.post()).willThrow(new RestClientException("AI 서버 연결 실패"));

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범답안", List.of(), "학생 답변", "CRITICAL");

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.feedback()).contains("실패");
        assertThat(result.errorTypes()).isEmpty();
    }

    @Test
    @DisplayName("폴백 시 foundKeywords 빈 리스트, missingKeywords 빈 리스트")
    void scoreAnswer_fallback_returnsEmptyKeywordLists() {
        given(aiRestClient.post()).willThrow(new RestClientException("연결 오류"));

        AiGradingResult result = service.scoreAnswer(
                "지문", "문제", "모범답안", List.of(), "답변", "INFERENTIAL");

        assertThat(result.foundKeywords()).isEmpty();
        assertThat(result.missingKeywords()).isEmpty();
    }
}
