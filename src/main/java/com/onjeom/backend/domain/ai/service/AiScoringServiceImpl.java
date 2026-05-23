package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.KeywordDto;
import com.onjeom.backend.domain.ai.dto.WritingEvaluateRequest;
import com.onjeom.backend.domain.ai.dto.WritingEvaluateResponse;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiScoringServiceImpl implements AiScoringService {

    private final RestClient aiRestClient;

    @Override
    public int scoreAnswer(String passageText, String questionText,
                           String modelAnswer, String userAnswer,
                           List<ProblemKeyword> keywords) {
        List<KeywordDto> keywordDtos = (keywords == null ? Collections.<ProblemKeyword>emptyList() : keywords)
                .stream()
                .map(k -> new KeywordDto(k.getKeyword(), k.getWeight()))
                .toList();

        WritingEvaluateRequest request = new WritingEvaluateRequest(
                passageText, questionText, modelAnswer, userAnswer, keywordDtos
        );

        try {
            WritingEvaluateResponse response = aiRestClient.post()
                    .uri("/api/writing/evaluate")
                    .body(request)
                    .retrieve()
                    .body(WritingEvaluateResponse.class);

            if (response == null) {
                log.warn("AI API 응답이 null입니다. 기본값 50 반환");
                return 50;
            }

            log.info("AI 채점 완료 - finalScore={}, feedbackType={}",
                    response.finalScore(), response.feedbackType());
            return response.finalScore();

        } catch (RestClientException e) {
            log.error("AI API 호출 실패, 폴백 점수 반환: {}", e.getMessage());
            return new Random().nextInt(41) + 50;
        }
    }
}
