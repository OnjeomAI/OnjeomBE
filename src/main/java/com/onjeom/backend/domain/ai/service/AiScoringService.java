package com.onjeom.backend.domain.ai.service;

public interface AiScoringService {

    int scoreAnswer(String passageText, String questionText,
                    String modelAnswer, String userAnswer);
}
