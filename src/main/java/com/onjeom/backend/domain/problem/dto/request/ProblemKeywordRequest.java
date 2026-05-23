package com.onjeom.backend.domain.problem.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProblemKeywordRequest(
        @NotBlank @Size(max = 100) String keyword,
        @NotNull @Min(1) @Max(100) Integer weight
) {}
