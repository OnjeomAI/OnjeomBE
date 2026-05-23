package com.onjeom.backend.domain.writing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompetencyHistoryItem(
        @NotBlank String competency,
        @NotEmpty List<Integer> scores
) {}
