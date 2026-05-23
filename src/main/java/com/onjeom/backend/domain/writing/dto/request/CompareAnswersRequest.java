package com.onjeom.backend.domain.writing.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompareAnswersRequest(
        @NotNull Long problemId,
        @NotBlank String previousAnswer,
        @NotNull @Min(0) @Max(100) Integer previousScore,
        @NotBlank String currentAnswer,
        @NotNull @Min(0) @Max(100) Integer currentScore
) {}
