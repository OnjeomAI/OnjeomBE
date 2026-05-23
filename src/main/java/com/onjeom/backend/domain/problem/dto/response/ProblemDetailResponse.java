package com.onjeom.backend.domain.problem.dto.response;

import com.onjeom.backend.domain.problem.enums.ProblemType;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ProblemDetailResponse(
        Long id,
        String passageText,
        String questionText,
        ProblemType problemType,
        ReadingType readingType,
        Integer difficulty,
        String modelAnswer,
        Boolean vectorIndexed,
        VectorIndexStatus vectorIndexStatus,
        List<ProblemKeywordResponse> keywords,
        LocalDateTime createdAt
) {}
