package com.onjeom.backend.domain.response.dto.response;

import com.onjeom.backend.domain.response.entity.Response;

import java.time.LocalDateTime;
import java.util.List;

public record ResponseDetailResponse(
        Long id,
        Long problemId,
        String answerText,
        int rawScore,
        int finalScore,
        String feedbackText,
        String scoringBasis,
        List<String> foundKeywords,
        int attemptNumber,
        LocalDateTime createdAt
) {
    public static ResponseDetailResponse from(Response response, List<String> foundKeywords) {
        return new ResponseDetailResponse(
                response.getId(),
                response.getProblem().getId(),
                response.getAnswerText(),
                response.getRawScore(),
                response.getFinalScore(),
                response.getFeedbackText(),
                response.getScoringBasis(),
                foundKeywords,
                response.getAttemptNumber(),
                response.getCreatedAt()
        );
    }
}
