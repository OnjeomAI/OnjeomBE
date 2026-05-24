package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record CurriculumPlanAiRequest(
        @JsonProperty("theta") double theta,
        @JsonProperty("daily_goal") int dailyGoal,
        @JsonProperty("competency_scores") Map<String, Integer> competencyScores,
        @JsonProperty("available_problems") List<AvailableProblemDto> availableProblems
) {}
