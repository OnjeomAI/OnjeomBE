package com.onjeom.backend.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiGradingServiceImpl implements AiGradingService {

    private final RestClient aiRestClient;

    @Override
    public AiGradingResult grade(String passageText, String questionText,
                                 String modelAnswer, List<AiKeyword> keywords,
                                 String userAnswer) {
        record GradeRequest(String passage, String question, String model_answer,
                            List<AiKeyword> keywords, String student_answer) {}

        try {
            AiGradingResponse response = aiRestClient.post()
                    .uri("/api/grading/grade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GradeRequest(passageText, questionText, modelAnswer, keywords, userAnswer))
                    .retrieve()
                    .body(AiGradingResponse.class);

            if (response == null) {
                log.error("AI 채점 서버 응답 없음, fallback 반환");
                return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 연결 실패", "");
            }

            log.info("AI 진단 채점 완료 - score={}", response.score());
            return new AiGradingResult(
                    response.score(),
                    response.stage1_score(),
                    response.found_keywords(),
                    response.missing_keywords().stream()
                            .map(k -> new AiKeyword(k.keyword(), k.weight()))
                            .toList(),
                    response.feedback(),
                    response.grade_reason()
            );
        } catch (Exception e) {
            log.error("AI 채점 서버 호출 실패, fallback 반환: {}", e.getMessage());
            return new AiGradingResult(50, 50, List.of(), List.of(), "채점 서버 연결 실패", "");
        }
    }

    private record AiGradingResponse(
            int score,
            int stage1_score,
            List<String> found_keywords,
            List<AiKeyword> missing_keywords,
            String feedback,
            String grade_reason
    ) {}
}
