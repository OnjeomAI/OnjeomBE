package com.onjeom.backend.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AiScoringServiceMock implements AiScoringService {

    @Override
    public AiGradingResult scoreAnswer(String passageText, String questionText,
                                       String modelAnswer, List<AiKeyword> keywords,
                                       String userAnswer) {
        int score = new Random().nextInt(41) + 50;
        log.info("[MOCK] AI 채점 생략 - 임시 점수 반환: {}", score);
        return new AiGradingResult(score, score, List.of(), List.of(), "[MOCK] 피드백 없음");
    }
}
