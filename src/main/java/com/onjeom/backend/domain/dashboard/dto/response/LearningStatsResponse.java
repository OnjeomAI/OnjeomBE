package com.onjeom.backend.domain.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;

public record LearningStatsResponse(
        int totalResponses,
        double averageScore,
        int streakDays,
        List<StatPoint> recentStats
) {
    public record StatPoint(
            LocalDate date,
            int count,
            double averageScore
    ) {}
}
