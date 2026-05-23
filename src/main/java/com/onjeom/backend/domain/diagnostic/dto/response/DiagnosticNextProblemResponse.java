package com.onjeom.backend.domain.diagnostic.dto.response;

public record DiagnosticNextProblemResponse(
        boolean isCompleted,
        DiagnosticProblemResponse nextProblem,
        DiagnosticResultResponse result
) {
}
