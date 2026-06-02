package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.AvailableProblemDto;
import com.onjeom.backend.domain.ai.dto.CurriculumPlanAiRequest;
import com.onjeom.backend.domain.ai.dto.CurriculumPlanAiResponse;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.server.impl", havingValue = "writing", matchIfMissing = true)
public class AiCurriculumServiceWritingImpl implements AiCurriculumService {

    private final RestClient aiRestClient;
    private final ProblemRepository problemRepository;

    @Override
    public Map<Integer, List<Long>> generateCurriculumPlan(
            BigDecimal theta, int dailyGoal, Map<String, Integer> competencyScores) {

        List<AvailableProblemDto> availableProblems = problemRepository.findAll().stream()
                .map(p -> new AvailableProblemDto(p.getId(), p.getDifficulty(), p.getReadingType().name()))
                .toList();

        CurriculumPlanAiRequest request = new CurriculumPlanAiRequest(
                theta.doubleValue(),
                dailyGoal,
                competencyScores,
                availableProblems
        );

        try {
            CurriculumPlanAiResponse response = aiRestClient.post()
                    .uri("/api/writing/curriculum-plan")
                    .body(request)
                    .retrieve()
                    .body(CurriculumPlanAiResponse.class);

            if (response == null || response.plan() == null) {
                log.warn("AI 커리큘럼 API 응답이 null입니다. 기본 플랜으로 대체");
                return fallback(theta, dailyGoal);
            }

            log.info("AI 커리큘럼 플랜 생성 완료 - stages={}", response.plan().keySet());
            return response.plan();

        } catch (RestClientException e) {
            log.error("AI 커리큘럼 API 호출 실패, 기본 플랜으로 대체: {}", e.getMessage());
            return fallback(theta, dailyGoal);
        }
    }

    private Map<Integer, List<Long>> fallback(BigDecimal theta, int dailyGoal) {
        double t = theta.doubleValue();
        List<Integer> stages;
        if (t < -0.5)     stages = List.of(1);
        else if (t < 0.0) stages = List.of(1, 2);
        else if (t < 0.5) stages = List.of(2, 3);
        else               stages = List.of(3, 4);

        int perStage = Math.max(dailyGoal * 7, 1);
        Map<Integer, List<Long>> plan = new LinkedHashMap<>();
        for (int stage : stages) {
            ReadingType rt = STAGE_TO_READING_TYPE.get(stage);
            List<Long> ids = problemRepository.findByReadingType(rt)
                    .stream().map(Problem::getId).toList();
            if (!ids.isEmpty()) {
                plan.put(stage, ids.subList(0, Math.min(perStage, ids.size())));
            }
        }

        int nextStage = plan.size() + 1;
        for (ReadingType rt : List.of(ReadingType.VOCABULARY, ReadingType.LOGICAL)) {
            List<Long> ids = problemRepository.findByReadingType(rt)
                    .stream().map(Problem::getId).toList();
            if (!ids.isEmpty()) {
                plan.put(nextStage++, ids.subList(0, Math.min(perStage, ids.size())));
            }
        }
        return plan;
    }

    private static final Map<Integer, ReadingType> STAGE_TO_READING_TYPE = Map.of(
            1, ReadingType.FACTUAL,
            2, ReadingType.INFERENTIAL,
            3, ReadingType.CRITICAL,
            4, ReadingType.CREATIVE
    );
}
