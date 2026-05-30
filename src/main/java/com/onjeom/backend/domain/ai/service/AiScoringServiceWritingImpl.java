package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.KeywordDto;
import com.onjeom.backend.domain.ai.dto.WritingEvaluateRequest;
import com.onjeom.backend.domain.ai.dto.WritingEvaluateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.server.impl", havingValue = "writing", matchIfMissing = true)
public class AiScoringServiceWritingImpl implements AiScoringService {

    private final RestClient aiRestClient;

    @Override
    public AiGradingResult scoreAnswer(String passageText, String questionText,
                                       String modelAnswer, List<AiKeyword> keywords,
                                       String userAnswer) {
        List<KeywordDto> keywordDtos = (keywords == null ? List.<AiKeyword>of() : keywords)
                .stream()
                .map(k -> new KeywordDto(k.keyword(), k.weight()))
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
                log.warn("AI API 응답이 null입니다. 기본값 반환");
                return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 응답 없음", "");
            }

            log.info("AI 채점 완료 - finalScore={}, feedbackType={}", response.finalScore(), response.feedbackType());

            int stage1Score = response.keywordScore() != null ? response.keywordScore() : response.finalScore();
            List<AiKeyword> missingKeywords = response.missingKeywords() == null ? List.of()
                    : response.missingKeywords().stream().map(k -> new AiKeyword(k, 0)).toList();

            return new AiGradingResult(
                    response.finalScore(),
                    stage1Score,
                    response.matchedKeywords() != null ? response.matchedKeywords() : List.of(),
                    missingKeywords,
                    response.feedback(),
                    response.feedbackType()
            );

        } catch (RestClientException e) {
            log.error("AI API 호출 실패, 폴백 반환: {}", e.getMessage());
            return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 연결 실패", "");
        }
    }
}
