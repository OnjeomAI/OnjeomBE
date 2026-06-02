package com.onjeom.backend.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.server.impl", havingValue = "grade")
public class AiScoringServiceGradeImpl implements AiScoringService {

    private final RestClient restClient;

    public AiScoringServiceGradeImpl(@Value("${ai.server.url}") String aiServerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .build();
    }

    @Override
    public AiGradingResult scoreAnswer(String passageText, String questionText,
                                       String modelAnswer, List<AiKeyword> keywords,
                                       String userAnswer, String readingType) {
        record GradeRequest(String passage, String question, String model_answer,
                            List<AiKeyword> keywords, String student_answer, String reading_type) {}

        try {
            AiGradingResponse response = restClient.post()
                    .uri("/api/grading/grade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GradeRequest(passageText, questionText, modelAnswer, keywords, userAnswer, readingType))
                    .retrieve()
                    .body(AiGradingResponse.class);

            if (response == null) {
                log.error("AI 채점 서버 응답 없음, fallback 반환");
                return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 연결 실패", "", List.of());
            }

            return new AiGradingResult(
                    response.score(),
                    response.stage1_score(),
                    response.found_keywords(),
                    response.missing_keywords(),
                    response.feedback(),
                    "",
                    List.of()
            );
        } catch (Exception e) {
            log.error("AI 채점 서버 호출 실패, fallback 반환: {}", e.getMessage());
            return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 연결 실패", "", List.of());
        }
    }

    private record AiGradingResponse(
            int score,
            int stage1_score,
            List<String> found_keywords,
            List<AiKeyword> missing_keywords,
            String feedback
    ) {}
}
