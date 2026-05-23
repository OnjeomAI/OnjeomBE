package com.onjeom.backend.domain.problem.dto.request;

import com.onjeom.backend.domain.problem.enums.ReadingType;

public record UpdateProblemRequest(
        String passageText,
        String questionText,
        ReadingType readingType,
        Integer difficulty,
        String modelAnswer
) {}
