package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record CurriculumPlanAiResponse(
        @JsonProperty("plan") Map<Integer, List<Long>> plan
) {}
