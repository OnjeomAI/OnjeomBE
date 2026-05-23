package com.onjeom.backend.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@Primary
public class AiScoringServiceMock implements AiScoringService {

    @Override
    public int scoreAnswer(String passageText, String questionText,
                           String modelAnswer, String userAnswer) {
        // TODO: 파인튜닝 모델 API 연동 — STEP 6에서 구현
        int score = new Random().nextInt(41) + 50;
        log.info("[MOCK] AI 채점 생략 - 임시 점수 반환: {}", score);
        return score;
    }
}
