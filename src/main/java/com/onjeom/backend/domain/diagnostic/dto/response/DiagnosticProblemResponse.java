package com.onjeom.backend.domain.diagnostic.dto.response;

public record DiagnosticProblemResponse(
        Long problemId,
        Integer questionNumber,
        String passageText,
        String questionText,
        Integer totalQuestions
) {
}
