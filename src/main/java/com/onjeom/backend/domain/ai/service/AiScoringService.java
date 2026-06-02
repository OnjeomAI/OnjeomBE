package com.onjeom.backend.domain.ai.service;

import java.util.List;

public interface AiScoringService {

    AiGradingResult scoreAnswer(String passageText, String questionText,
                                String modelAnswer, List<AiKeyword> keywords,
                                String userAnswer, String readingType);
}
