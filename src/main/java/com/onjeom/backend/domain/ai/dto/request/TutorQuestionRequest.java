package com.onjeom.backend.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TutorQuestionRequest(
        @NotBlank String question,
        Long problemId,
        String passageText
) {
}
