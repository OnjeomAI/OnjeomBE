package com.onjeom.backend.domain.cms.dto.request;

import com.onjeom.backend.domain.problem.enums.ReadingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateProblemRequest(
        @NotNull @Min(1) @Max(5) Integer difficulty,
        @NotNull @Schema(allowableValues = {"FACTUAL", "INFERENTIAL", "CRITICAL", "CREATIVE"}) ReadingType readingType,
        String topic
) {}
