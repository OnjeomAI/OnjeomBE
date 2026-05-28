package com.onjeom.backend.domain.learning.dto.response;

import com.onjeom.backend.domain.learning.enums.CompetencyLevel;
import com.onjeom.backend.domain.learning.enums.CompetencyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompetencyScoreResponse(
        CompetencyType competencyType,
        BigDecimal score,
        CompetencyLevel level,
        BigDecimal delta,
        LocalDateTime measuredAt
) {
}
