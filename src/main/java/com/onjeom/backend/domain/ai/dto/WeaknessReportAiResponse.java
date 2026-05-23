package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WeaknessReportAiResponse(
        @JsonProperty("weak_competencies")  List<WeakCompetencyDetailDto> weakCompetencies,
        @JsonProperty("report")             String report,
        @JsonProperty("recommendations")    List<String> recommendations,
        @JsonProperty("priority_competency") String priorityCompetency
) {}
