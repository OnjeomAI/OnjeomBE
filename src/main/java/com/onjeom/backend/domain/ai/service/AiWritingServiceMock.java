package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.CompareAnswersAiRequest;
import com.onjeom.backend.domain.ai.dto.CompareAnswersAiResponse;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiRequest;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiResponse;
import com.onjeom.backend.domain.ai.dto.WeakCompetencyDetailDto;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiRequest;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "false", matchIfMissing = true)
public class AiWritingServiceMock implements AiWritingService {

    @Override
    public CurriculumAdjustAiResponse adjustCurriculum(CurriculumAdjustAiRequest request) {
        log.info("[MOCK] adjustCurriculum 호출");
        boolean hasWeak = request.competencyHistory().stream()
                .anyMatch(h -> {
                    List<Integer> recent = h.scores().size() >= 3
                            ? h.scores().subList(h.scores().size() - 3, h.scores().size())
                            : h.scores();
                    return !recent.isEmpty() && recent.stream().allMatch(s -> s < 50);
                });

        return new CurriculumAdjustAiResponse(
                hasWeak,
                List.of(),
                hasWeak ? "일부 역량 점수가 낮아요. 관련 문제를 먼저 배치했어요." : "현재 학습 경로가 적절합니다.",
                "부족한 역량을 집중적으로 연습하는 것을 권장합니다."
        );
    }

    @Override
    public CompareAnswersAiResponse compareAnswers(CompareAnswersAiRequest request) {
        log.info("[MOCK] compareAnswers 호출");
        int diff = request.currentScore() - request.previousScore();
        String msg = diff > 0
                ? "이번엔 점수가 " + diff + "점 올랐어요. 꾸준히 성장하고 있어요!"
                : diff == 0 ? "점수가 유지됐어요. 조금 더 노력해보세요."
                : "이번엔 점수가 내려갔어요. 다시 한번 개념을 확인해보세요.";
        return new CompareAnswersAiResponse(diff, diff > 0, msg, List.of(), List.of(), "이전 답변과 현재 답변을 비교했습니다.");
    }

    @Override
    public WeaknessReportAiResponse generateWeaknessReport(WeaknessReportAiRequest request) {
        log.info("[MOCK] generateWeaknessReport 호출");
        List<WeakCompetencyDetailDto> weak = request.competencyScores().stream()
                .filter(c -> c.score() < 70)
                .map(c -> new WeakCompetencyDetailDto(
                        c.competency(), c.score(), c.score() < 50 ? "취약" : "보통"))
                .toList();

        String priority = weak.stream()
                .min(java.util.Comparator.comparingInt(WeakCompetencyDetailDto::score))
                .map(WeakCompetencyDetailDto::competency)
                .orElse(null);

        return new WeaknessReportAiResponse(
                weak,
                weak.isEmpty() ? "전반적으로 양호합니다." : "일부 역량에서 집중적인 학습이 필요합니다.",
                List.of("관련 문제를 꾸준히 풀어보세요."),
                priority
        );
    }
}
