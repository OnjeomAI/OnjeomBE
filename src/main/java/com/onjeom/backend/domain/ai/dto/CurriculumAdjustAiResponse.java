package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CurriculumAdjustAiResponse(
        @JsonProperty("needs_adjustment")   boolean needsAdjustment,
        @JsonProperty("weak_competencies")  List<String> weakCompetencies,
        @JsonProperty("adjustment_message") String adjustmentMessage,
        @JsonProperty("recommended_focus")  String recommendedFocus
) {}
