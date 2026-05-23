package com.onjeom.backend.domain.curriculum.dto.response;

public record CurriculumProgressResponse(
        Long curriculumId,
        Integer currentStage,
        int totalItems,
        int completedItems,
        int skippedItems,
        double progressPercent
) {
}
