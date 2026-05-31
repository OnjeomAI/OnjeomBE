package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeepAnalysisDto(
        @JsonProperty("error_types") List<String> errorTypes,
        @JsonProperty("analysis")    String analysis,
        @JsonProperty("improvement") String improvement
) {}
