package com.onjeom.backend.domain.ai.dto.response;

import java.util.List;

public record TutorAnswerResponse(
        String answer,
        List<String> references
) {
}
