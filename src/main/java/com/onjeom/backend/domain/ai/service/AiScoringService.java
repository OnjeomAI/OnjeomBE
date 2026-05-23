package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.problem.entity.ProblemKeyword;

import java.util.Collections;
import java.util.List;

public interface AiScoringService {

    int scoreAnswer(String passageText, String questionText,
                    String modelAnswer, String userAnswer,
                    List<ProblemKeyword> keywords);

    default int scoreAnswer(String passageText, String questionText,
                            String modelAnswer, String userAnswer) {
        return scoreAnswer(passageText, questionText, modelAnswer, userAnswer, Collections.emptyList());
    }
}
