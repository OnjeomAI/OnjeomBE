package com.onjeom.backend.domain.dashboard.dto.response;

import com.onjeom.backend.domain.learning.enums.CompetencyType;

import java.util.List;

public record AdminStatsResponse(
        long totalUsers,
        long newUsersThisMonth,
        long totalResponses,
        long activeUsersThisMonth,
        double overallAverageScore,
        List<CompetencyWeakStat> competencyStats
) {
    public record CompetencyWeakStat(
            CompetencyType type,
            double averageScore,
            long userCount
    ) {}
}
