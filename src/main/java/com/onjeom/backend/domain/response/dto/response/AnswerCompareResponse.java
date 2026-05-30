package com.onjeom.backend.domain.response.dto.response;

import com.onjeom.backend.domain.problem.dto.response.ProblemKeywordResponse;

import java.util.List;

public record AnswerCompareResponse(
        Long responseId,
        Long problemId,
        String passageText,
        String questionText,
        String studentAnswer,
        String modelAnswer,
        int finalScore,
        String feedbackText,
        List<ProblemKeywordResponse> keywords
) {}
