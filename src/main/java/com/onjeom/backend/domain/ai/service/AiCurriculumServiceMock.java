package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCurriculumServiceMock implements AiCurriculumService {

    private final ProblemRepository problemRepository;

    @Override
    public Map<Integer, List<Long>> generateCurriculumPlan(
            BigDecimal theta, int dailyGoal, Map<String, Integer> competencyScores) {
        log.info("[MOCK] 커리큘럼 생성 생략 - 임시 플랜 반환");

        List<Problem> allProblems = new ArrayList<>(problemRepository.findAll());
        Collections.shuffle(allProblems);

        List<Integer> stages = determineStages(theta);
        int perStage = dailyGoal * 7;

        Map<Integer, List<Long>> plan = new LinkedHashMap<>();
        int offset = 0;
        for (Integer stage : stages) {
            if (offset >= allProblems.size()) break;
            int end = Math.min(offset + perStage, allProblems.size());
            List<Long> ids = allProblems.subList(offset, end).stream()
                    .map(Problem::getId)
                    .toList();
            plan.put(stage, ids);
            offset = end;
        }

        return plan;
    }

    private List<Integer> determineStages(BigDecimal theta) {
        double t = theta.doubleValue();
        if (t < -0.5) return List.of(1);
        if (t < 0.0)  return List.of(1, 2);
        if (t < 0.5)  return List.of(2, 3);
        return List.of(3, 4);
    }
}
