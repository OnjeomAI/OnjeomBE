package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeakCompetencyDetailDto(
        @JsonProperty("competency") String competency,
        @JsonProperty("score")      int score,
        @JsonProperty("level")      String level
) {}
