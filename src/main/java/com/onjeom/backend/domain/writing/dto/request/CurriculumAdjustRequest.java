package com.onjeom.backend.domain.writing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CurriculumAdjustRequest(
        @NotEmpty @Valid List<CompetencyHistoryItem> competencyHistory
) {}
