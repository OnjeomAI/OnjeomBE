package com.onjeom.backend.domain.ai.service;

<<<<<<< HEAD
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;

import java.util.Collections;
=======
>>>>>>> origin/develop
import java.util.List;

public interface AiScoringService {

<<<<<<< HEAD
    int scoreAnswer(String passageText, String questionText,
                    String modelAnswer, String userAnswer,
                    List<ProblemKeyword> keywords);

    default int scoreAnswer(String passageText, String questionText,
                            String modelAnswer, String userAnswer) {
        return scoreAnswer(passageText, questionText, modelAnswer, userAnswer, Collections.emptyList());
    }
=======
    AiGradingResult scoreAnswer(String passageText, String questionText,
                                String modelAnswer, List<AiKeyword> keywords,
                                String userAnswer);
>>>>>>> origin/develop
}
