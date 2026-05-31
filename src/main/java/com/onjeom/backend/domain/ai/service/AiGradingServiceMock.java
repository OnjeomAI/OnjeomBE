package com.onjeom.backend.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "false", matchIfMissing = true)
public class AiGradingServiceMock implements AiGradingService {

    @Override
    public AiGradingResult grade(String passageText, String questionText,
                                 String modelAnswer, List<AiKeyword> keywords,
                                 String userAnswer) {
        int score = new Random().nextInt(41) + 50;
        log.info("[MOCK] AI 진단 채점 생략 - 임시 점수 반환: {}", score);
        return new AiGradingResult(score, score, List.of(), List.of(), "[MOCK] 피드백 없음", "");
    }
}
