package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.request.TermExplainRequest;
import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.dto.response.TermExplainResponse;
import com.onjeom.backend.domain.ai.dto.response.TutorAnswerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Primary
@Service
public class AiTutorServiceImpl implements AiTutorService {

    private final RestClient restClient;

    public AiTutorServiceImpl(@Value("${ai.server.url}") String aiServerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .defaultHeader("ngrok-skip-browser-warning", "true")
                .build();
    }

    @Override
    public TutorAnswerResponse ask(Long userId, TutorQuestionRequest request) {
        record AiTutorRequest(String question) {}
        record AiTutorResponse(String answer, List<String> sources) {}

        try {
            AiTutorResponse response = restClient.post()
                    .uri("/api/tutor/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AiTutorRequest(request.question()))
                    .retrieve()
                    .body(AiTutorResponse.class);

            if (response == null) {
                log.error("AI 튜터 서버 응답 없음");
                return new TutorAnswerResponse("AI 튜터 서버 연결에 실패했습니다.", List.of());
            }

            return new TutorAnswerResponse(
                    response.answer(),
                    response.sources() != null ? response.sources() : List.of()
            );

        } catch (Exception e) {
            log.error("AI 튜터 서버 호출 실패: {}", e.getMessage());
            return new TutorAnswerResponse("AI 튜터 서버 연결에 실패했습니다.", List.of());
        }
    }

    @Override
    public TermExplainResponse explain(TermExplainRequest request) {
        record AiExplainRequest(String term, String passage_text) {}
        record AiExplainResponse(String term, String explanation) {}

        try {
            AiExplainResponse response = restClient.post()
                    .uri("/api/writing/explain-term")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AiExplainRequest(request.term(), request.passageText()))
                    .retrieve()
                    .body(AiExplainResponse.class);

            if (response == null) {
                log.error("AI 용어 설명 서버 응답 없음");
                return new TermExplainResponse(request.term(), "용어 설명 서버 연결에 실패했습니다.");
            }

            return new TermExplainResponse(response.term(), response.explanation());

        } catch (Exception e) {
            log.error("AI 용어 설명 서버 호출 실패: {}", e.getMessage());
            return new TermExplainResponse(request.term(), "용어 설명 서버 연결에 실패했습니다.");
        }
    }
}
