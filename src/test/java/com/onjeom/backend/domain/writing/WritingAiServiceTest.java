package com.onjeom.backend.domain.writing;

import com.onjeom.backend.domain.ai.dto.*;
import com.onjeom.backend.domain.ai.service.AiWritingService;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.writing.dto.request.CompareAnswersRequest;
import com.onjeom.backend.domain.writing.dto.request.CompetencyHistoryItem;
import com.onjeom.backend.domain.writing.dto.request.CompetencyScoreItem;
import com.onjeom.backend.domain.writing.dto.request.CurriculumAdjustRequest;
import com.onjeom.backend.domain.writing.dto.request.WeaknessReportRequest;
import com.onjeom.backend.domain.writing.service.WritingAiService;
import com.onjeom.backend.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WritingAiService — 글쓰기 AI 서비스 요청/응답 매핑 테스트")
class WritingAiServiceTest {

    @Mock AiWritingService aiWritingService;
    @Mock ProblemRepository problemRepository;
    @Mock ProblemKeywordRepository problemKeywordRepository;
    @InjectMocks WritingAiService writingAiService;

    private Problem mockProblem;

    @BeforeEach
    void setUp() {
        mockProblem = Problem.builder()
                .questionText("작가의 주장을 서술하시오.")
                .modelAnswer("작가는 자연보호의 필요성을 주장한다.")
                .build();
    }

    // ── adjustCurriculum ─────────────────────────────────────────────────────

    @Test
    @DisplayName("adjustCurriculum: 취약 역량 있을 때 needs_adjustment=true 반환")
    void adjustCurriculum_withWeakCompetency_returnsNeedsAdjustment() {
        given(aiWritingService.adjustCurriculum(any())).willReturn(
                new CurriculumAdjustAiResponse(true, List.of("추론적 독해"),
                        "추론 점수가 낮아요.", "추론 문제를 집중 연습하세요.")
        );

        var req = new CurriculumAdjustRequest(List.of(
                new CompetencyHistoryItem("inferential", List.of(30, 40, 45))
        ));
        var result = writingAiService.adjustCurriculum(req);

        assertThat(result.needsAdjustment()).isTrue();
        assertThat(result.weakCompetencies()).contains("추론적 독해");
        assertThat(result.adjustmentMessage()).isNotBlank();
    }

    @Test
    @DisplayName("adjustCurriculum: 역량 이력이 AI 서비스에 올바르게 전달됨")
    void adjustCurriculum_passesHistoryToAiService() {
        given(aiWritingService.adjustCurriculum(any())).willReturn(
                new CurriculumAdjustAiResponse(false, List.of(), "적절합니다.", "계속하세요.")
        );

        var req = new CurriculumAdjustRequest(List.of(
                new CompetencyHistoryItem("factual", List.of(80, 85, 90))
        ));
        writingAiService.adjustCurriculum(req);

        verify(aiWritingService).adjustCurriculum(any(CurriculumAdjustAiRequest.class));
    }

    // ── compareAnswers ───────────────────────────────────────────────────────

    @Test
    @DisplayName("compareAnswers: 점수 향상 시 is_improved=true 반환")
    void compareAnswers_withImprovedScore_returnsIsImproved() {
        given(problemRepository.findById(1L)).willReturn(Optional.of(mockProblem));
        given(problemKeywordRepository.findByProblemId(any())).willReturn(List.of());
        given(aiWritingService.compareAnswers(any())).willReturn(
                new CompareAnswersAiResponse(35, true, "성장했어요!",
                        List.of("자연보호"), List.of(), "구체성이 향상됐습니다.")
        );

        var req = new CompareAnswersRequest(1L, "이전 답변", 40, "현재 답변", 75);
        var result = writingAiService.compareAnswers(req);

        assertThat(result.isImproved()).isTrue();
        assertThat(result.scoreDiff()).isEqualTo(35);
        assertThat(result.growthMessage()).isNotBlank();
    }

    @Test
    @DisplayName("compareAnswers: 존재하지 않는 문제 ID면 예외 발생")
    void compareAnswers_withInvalidProblemId_throwsException() {
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        var req = new CompareAnswersRequest(999L, "이전", 40, "현재", 75);

        assertThatThrownBy(() -> writingAiService.compareAnswers(req))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("compareAnswers: DB 키워드를 AI 요청에 포함해서 전달")
    void compareAnswers_includesKeywordsFromDb() {
        var keyword = ProblemKeyword.builder().keyword("자연보호").weight(100).build();
        given(problemRepository.findById(1L)).willReturn(Optional.of(mockProblem));
        given(problemKeywordRepository.findByProblemId(any())).willReturn(List.of(keyword));
        given(aiWritingService.compareAnswers(any())).willReturn(
                new CompareAnswersAiResponse(10, true, "성장!", List.of(), List.of(), "분석")
        );

        writingAiService.compareAnswers(new CompareAnswersRequest(1L, "이전", 40, "현재", 50));

        verify(aiWritingService).compareAnswers(argThat(r ->
                r.keywords() != null && !r.keywords().isEmpty()
        ));
    }

    // ── generateWeaknessReport ───────────────────────────────────────────────

    @Test
    @DisplayName("generateWeaknessReport: AI 응답의 weak_competencies 목록 그대로 반환")
    void generateWeaknessReport_mapsWeakCompetencies() {
        given(aiWritingService.generateWeaknessReport(any())).willReturn(
                new WeaknessReportAiResponse(
                        List.of(new WeakCompetencyDetailDto("사실적 독해", 40, "취약")),
                        "집중 학습이 필요합니다.",
                        List.of("매일 2문제씩 풀어보세요."),
                        "사실적 독해"
                )
        );

        var req = new WeaknessReportRequest(List.of(
                new CompetencyScoreItem("factual", 40),
                new CompetencyScoreItem("inferential", 80)
        ));
        var result = writingAiService.generateWeaknessReport(req);

        assertThat(result.weakCompetencies()).hasSize(1);
        assertThat(result.weakCompetencies().get(0).competency()).isEqualTo("사실적 독해");
        assertThat(result.priorityCompetency()).isEqualTo("사실적 독해");
        assertThat(result.recommendations()).isNotEmpty();
    }

    // ── import helper ────────────────────────────────────────────────────────
    private static <T> org.mockito.ArgumentMatcher<T> argThat(
            java.util.function.Predicate<T> predicate) {
        return predicate::test;
    }
}
