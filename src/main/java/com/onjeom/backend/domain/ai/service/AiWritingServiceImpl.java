package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.CompareAnswersAiRequest;
import com.onjeom.backend.domain.ai.dto.CompareAnswersAiResponse;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiRequest;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiResponse;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiRequest;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiWritingServiceImpl implements AiWritingService {

    private final RestClient aiRestClient;

    @Override
    public CurriculumAdjustAiResponse adjustCurriculum(CurriculumAdjustAiRequest request) {
        try {
            CurriculumAdjustAiResponse response = aiRestClient.post()
                    .uri("/api/writing/curriculum/adjust")
                    .body(request)
                    .retrieve()
                    .body(CurriculumAdjustAiResponse.class);

            if (response == null) {
                log.warn("AI curriculum/adjust 응답이 null입니다. 기본값 반환");
                return fallbackCurriculumAdjust();
            }

            log.info("커리큘럼 재조정 완료 - needsAdjustment={}", response.needsAdjustment());
            return response;

        } catch (RestClientException e) {
            log.error("AI curriculum/adjust 호출 실패: {}", e.getMessage());
            return fallbackCurriculumAdjust();
        }
    }

    @Override
    public CompareAnswersAiResponse compareAnswers(CompareAnswersAiRequest request) {
        try {
            CompareAnswersAiResponse response = aiRestClient.post()
                    .uri("/api/writing/compare")
                    .body(request)
                    .retrieve()
                    .body(CompareAnswersAiResponse.class);

            if (response == null) {
                log.warn("AI compare 응답이 null입니다. 기본값 반환");
                return fallbackCompareAnswers(request);
            }

            log.info("답변 비교 완료 - scoreDiff={}, isImproved={}", response.scoreDiff(), response.isImproved());
            return response;

        } catch (RestClientException e) {
            log.error("AI compare 호출 실패: {}", e.getMessage());
            return fallbackCompareAnswers(request);
        }
    }

    @Override
    public WeaknessReportAiResponse generateWeaknessReport(WeaknessReportAiRequest request) {
        try {
            WeaknessReportAiResponse response = aiRestClient.post()
                    .uri("/api/writing/weakness-report")
                    .body(request)
                    .retrieve()
                    .body(WeaknessReportAiResponse.class);

            if (response == null) {
                log.warn("AI weakness-report 응답이 null입니다. 기본값 반환");
                return fallbackWeaknessReport();
            }

            log.info("약점 리포트 생성 완료 - priorityCompetency={}", response.priorityCompetency());
            return response;

        } catch (RestClientException e) {
            log.error("AI weakness-report 호출 실패: {}", e.getMessage());
            return fallbackWeaknessReport();
        }
    }

    private CurriculumAdjustAiResponse fallbackCurriculumAdjust() {
        return new CurriculumAdjustAiResponse(
                false, List.of(),
                "현재 학습 경로가 적절합니다. 꾸준히 학습을 이어나가세요!",
                "현재 역량 수준에 맞는 문제를 계속 풀어보세요."
        );
    }

    private CompareAnswersAiResponse fallbackCompareAnswers(CompareAnswersAiRequest request) {
        int diff = request.currentScore() - request.previousScore();
        String msg = diff > 0
                ? "이번엔 점수가 " + diff + "점 올랐어요. 꾸준히 성장하고 있어요!"
                : "이번엔 점수가 내려갔어요. 다시 한번 개념을 확인해보세요.";
        return new CompareAnswersAiResponse(
                diff, diff > 0, msg,
                List.of(), List.of(),
                "이전 답변과 현재 답변을 비교한 결과입니다."
        );
    }

    private WeaknessReportAiResponse fallbackWeaknessReport() {
        return new WeaknessReportAiResponse(
                List.of(), "역량 분석 중 오류가 발생했습니다.",
                List.of("전반적인 학습을 꾸준히 이어나가세요."), null
        );
    }
}
