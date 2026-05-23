package com.onjeom.backend.domain.curriculum.dto.response;

import com.onjeom.backend.domain.curriculum.enums.CurriculumStatus;

import java.util.List;

public record CurriculumResponse(
        Long curriculumId,
        CurriculumStatus status,
        Integer currentStage,
        int totalItems,
        int completedItems,
        List<CurriculumItemResponse> todayItems
) {
}
