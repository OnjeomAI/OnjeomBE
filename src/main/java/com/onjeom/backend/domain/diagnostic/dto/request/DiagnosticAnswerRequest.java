package com.onjeom.backend.domain.diagnostic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticAnswerRequest(
        @NotNull Long problemId,
        @NotBlank String answerText,
        @NotNull Integer responseTimeSec
) {
}
