package com.onjeom.backend.domain.learning.dto.response;

import com.onjeom.backend.domain.learning.enums.CompetencyType;

import java.time.LocalDateTime;

public record KnowledgeTracingResponse(
        CompetencyType competencyType,
        Double pKnow,
        LocalDateTime updatedAt
) {
}
