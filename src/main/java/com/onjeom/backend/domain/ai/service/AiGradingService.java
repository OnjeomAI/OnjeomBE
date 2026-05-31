package com.onjeom.backend.domain.ai.service;

import java.util.List;

public interface AiGradingService {

    AiGradingResult grade(String passageText, String questionText,
                          String modelAnswer, List<AiKeyword> keywords,
                          String userAnswer);
}
