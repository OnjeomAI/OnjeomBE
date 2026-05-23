package com.onjeom.backend.domain.cms.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCurriculumOrderRequest(
        @NotNull List<Long> problemIds
) {}
