package com.onjeom.backend.domain.curriculum.dto.response;

import com.onjeom.backend.domain.curriculum.enums.CurriculumItemStatus;
import com.onjeom.backend.domain.problem.enums.ReadingType;

import java.time.LocalDateTime;

public record CurriculumItemResponse(
        Long itemId,
        Long problemId,
        String questionText,
        ReadingType readingType,
        Integer difficulty,
        Integer stage,
        Integer orderIndex,
        CurriculumItemStatus status,
        LocalDateTime scheduledAt
) {
}
