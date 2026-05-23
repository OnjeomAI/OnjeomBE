package com.onjeom.backend.domain.problem.dto.response;

public record ProblemKeywordResponse(
        Long id,
        String keyword,
        Integer weight
) {}
