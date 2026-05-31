package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CurriculumAdjustAiRequest(
        @JsonProperty("competency_history") List<CompetencyHistoryDto> competencyHistory
) {}
