package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AvailableProblemDto(
        @JsonProperty("id") Long id,
        @JsonProperty("difficulty") int difficulty,
        @JsonProperty("reading_type") String readingType
) {}
