package com.onjeom.backend.domain.cms.dto.response;

import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;

import java.time.LocalDateTime;

public record CmsProblemsResponse(
        Long id,
        String questionText,
        ReadingType readingType,
        Integer difficulty,
        VectorIndexStatus vectorIndexStatus,
        int keywordCount,
        LocalDateTime createdAt
) {}
