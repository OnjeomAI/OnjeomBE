package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IrtEstimateResponse(
        @JsonProperty("theta")           BigDecimal theta,
        @JsonProperty("se")              BigDecimal se,
        @JsonProperty("ability_level")   String abilityLevel,
        @JsonProperty("next_difficulty") Integer nextDifficulty
) {}
