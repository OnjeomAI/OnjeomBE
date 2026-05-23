package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompetencyScoreDto(
        @JsonProperty("competency") String competency,
        @JsonProperty("score")      int score
) {}
