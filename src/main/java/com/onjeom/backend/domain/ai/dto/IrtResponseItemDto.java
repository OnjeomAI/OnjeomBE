package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IrtResponseItemDto(
        @JsonProperty("difficulty") Integer difficulty,
        @JsonProperty("score")      Integer score,
        @JsonProperty("a_param")    BigDecimal aParam,
        @JsonProperty("b_param")    BigDecimal bParam,
        @JsonProperty("c_param")    BigDecimal cParam
) {}
