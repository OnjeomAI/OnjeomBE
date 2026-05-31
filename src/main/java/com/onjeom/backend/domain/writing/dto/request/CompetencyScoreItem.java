package com.onjeom.backend.domain.writing.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompetencyScoreItem(
        @NotBlank String competency,
        @NotNull @Min(0) @Max(100) Integer score
) {}
