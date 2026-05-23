package com.onjeom.backend.domain.ai.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AiCurriculumService {

    Map<Integer, List<Long>> generateCurriculumPlan(
            BigDecimal theta,
            int dailyGoal,
            Map<String, Integer> competencyScores
    );
}
