package com.onjeom.backend.domain.dashboard.dto.response;

import com.onjeom.backend.domain.learning.enums.CompetencyLevel;
import com.onjeom.backend.domain.learning.enums.CompetencyType;

import java.math.BigDecimal;
import java.util.List;

public record WeakPointReportResponse(
        List<WeakCompetency> weakCompetencies,
        int reviewDueCount
) {
    public record WeakCompetency(
            CompetencyType type,
            BigDecimal score,
            CompetencyLevel level
    ) {}
}
