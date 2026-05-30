package com.onjeom.backend.domain.ai.dto.response;

public record TermExplainResponse(
        String term,
        String explanation
) {}
