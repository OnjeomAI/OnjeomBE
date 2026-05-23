package com.onjeom.backend.domain.problem.dto.response;

import com.onjeom.backend.domain.problem.enums.ProblemType;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;

import java.time.LocalDateTime;

public record ProblemResponse(
        Long id,
        String questionText,
        ProblemType problemType,
        ReadingType readingType,
        Integer difficulty,
        VectorIndexStatus vectorIndexStatus,
        LocalDateTime createdAt
) {}
