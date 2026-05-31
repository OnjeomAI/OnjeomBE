package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WeaknessReportAiRequest(
        @JsonProperty("competency_scores") List<CompetencyScoreDto> competencyScores
) {}
