package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "false", matchIfMissing = true)
public class AiScoringServiceMock implements AiScoringService {

    @Override
    public int scoreAnswer(String passageText, String questionText,
                           String modelAnswer, String userAnswer,
                           List<ProblemKeyword> keywords) {
        int score = new Random().nextInt(41) + 50;
        log.info("[MOCK] AI 채점 생략 - 임시 점수 반환: {}", score);
        return score;
    }
}
