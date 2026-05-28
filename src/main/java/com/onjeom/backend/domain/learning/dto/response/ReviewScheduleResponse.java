package com.onjeom.backend.domain.learning.dto.response;

import com.onjeom.backend.domain.problem.enums.ReadingType;

import java.time.LocalDateTime;

public record ReviewScheduleResponse(
        Long scheduleId,
        Long problemId,
        String questionText,
        ReadingType readingType,
        Integer reviewCount,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt
) {
}
