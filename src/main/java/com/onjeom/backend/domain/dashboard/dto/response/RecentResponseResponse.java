package com.onjeom.backend.domain.dashboard.dto.response;

import com.onjeom.backend.domain.problem.enums.ReadingType;

import java.time.LocalDateTime;
import java.util.List;

public record RecentResponseResponse(
        List<ResponseSummary> responses,
        int totalCount,
        int currentPage,
        int totalPages
) {
    public record ResponseSummary(
            Long responseId,
            Long problemId,
            String questionText,
            ReadingType readingType,
            int finalScore,
            LocalDateTime createdAt
    ) {}
}
