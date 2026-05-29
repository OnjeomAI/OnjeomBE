package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class AiCurriculumServiceImpl implements AiCurriculumService {

    private final RestClient restClient;
    private final ProblemRepository problemRepository;

    public AiCurriculumServiceImpl(
            @Value("${ai.server.url}") String aiServerUrl,
            ProblemRepository problemRepository) {
        this.restClient = RestClient.builder().baseUrl(aiServerUrl).build();
        this.problemRepository = problemRepository;
    }

    @Override
    public Map<Integer, List<Long>> generateCurriculumPlan(
            BigDecimal theta, int dailyGoal, Map<String, Integer> competencyScores) {

        List<String> weakAreas = competencyScores.entrySet().stream()
                .filter(e -> e.getValue() < 60 && COMPETENCY_TO_FOCUS.containsKey(e.getKey()))
                .map(e -> COMPETENCY_TO_FOCUS.get(e.getKey()))
                .distinct()
                .toList();

        record CurriculumRequest(BigDecimal theta, int daily_goal, List<String> weak_areas) {}

        try {
            AiCurriculumResponse response = restClient.post()
                    .uri("/api/curriculum/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CurriculumRequest(theta, dailyGoal, weakAreas))
                    .retrieve()
                    .body(AiCurriculumResponse.class);

            if (response == null || response.plans() == null || response.plans().isEmpty()) {
                log.warn("AI 커리큘럼 응답 비어있음, 기본 플랜으로 대체");
                return fallback(theta, dailyGoal);
            }

            return buildPlan(response.plans(), dailyGoal);

        } catch (Exception e) {
            log.error("AI 커리큘럼 서버 호출 실패, 기본 플랜으로 대체: {}", e.getMessage());
            return fallback(theta, dailyGoal);
        }
    }

    private Map<Integer, List<Long>> buildPlan(List<DayPlan> plans, int dailyGoal) {
        Map<Integer, Integer> daysPerStage = new LinkedHashMap<>();
        for (DayPlan plan : plans) {
            int stage = FOCUS_TO_STAGE.getOrDefault(plan.focus_area(), 1);
            daysPerStage.merge(stage, 1, Integer::sum);
        }

        Map<Integer, List<Long>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : daysPerStage.entrySet()) {
            int stage = entry.getKey();
            int needed = entry.getValue() * dailyGoal;
            ReadingType readingType = STAGE_TO_READING_TYPE.get(stage);

            List<Long> problemIds = problemRepository.findByReadingType(readingType)
                    .stream().map(Problem::getId).toList();

            if (problemIds.isEmpty()) continue;

            List<Long> selected = new ArrayList<>();
            while (selected.size() < needed) selected.addAll(problemIds);
            result.put(stage, selected.subList(0, needed));
        }
        return result;
    }

    private Map<Integer, List<Long>> fallback(BigDecimal theta, int dailyGoal) {
        double t = theta.doubleValue();
        List<Integer> stages;
        if (t < -0.5)      stages = List.of(1);
        else if (t < 0.0)  stages = List.of(1, 2);
        else if (t < 0.5)  stages = List.of(2, 3);
        else               stages = List.of(3, 4);

        int perStage = dailyGoal * 7;
        Map<Integer, List<Long>> plan = new LinkedHashMap<>();
        for (int stage : stages) {
            ReadingType rt = STAGE_TO_READING_TYPE.get(stage);
            List<Long> ids = problemRepository.findByReadingType(rt)
                    .stream().map(Problem::getId).toList();
            plan.put(stage, ids.subList(0, Math.min(perStage, ids.size())));
        }
        return plan;
    }

    private static final Map<String, String> COMPETENCY_TO_FOCUS = Map.of(
            "factual",     "사실적 이해",
            "inferential", "추론적 이해",
            "critical",    "비판적 이해"
    );

    private static final Map<String, Integer> FOCUS_TO_STAGE = Map.of(
            "사실적 이해", 1,
            "추론적 이해", 2,
            "비판적 이해", 3,
            "창의적 이해", 4
    );

    private static final Map<Integer, ReadingType> STAGE_TO_READING_TYPE = Map.of(
            1, ReadingType.FACTUAL,
            2, ReadingType.INFERENTIAL,
            3, ReadingType.CRITICAL,
            4, ReadingType.CREATIVE
    );

    private record AiCurriculumResponse(List<DayPlan> plans) {}
    private record DayPlan(String focus_area) {}
}
