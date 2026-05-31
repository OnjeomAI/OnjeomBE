package com.onjeom.backend.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TermExplainRequest(
        @NotBlank String term,
        String passageText
) {}
