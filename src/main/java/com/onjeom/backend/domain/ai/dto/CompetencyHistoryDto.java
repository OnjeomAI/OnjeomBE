package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompetencyHistoryDto(
        @JsonProperty("competency") String competency,
        @JsonProperty("scores")     List<Integer> scores
) {}
