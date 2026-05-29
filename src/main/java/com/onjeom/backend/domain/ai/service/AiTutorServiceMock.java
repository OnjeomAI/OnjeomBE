package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.dto.response.TutorAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTutorServiceMock implements AiTutorService {

    @Override
    public TutorAnswerResponse ask(Long userId, TutorQuestionRequest request) {
        log.info("[MOCK] AI 튜터 호출 생략 - question: {}", request.question());
        return new TutorAnswerResponse(
                "AI 튜터가 준비 중입니다. 잠시 후 다시 시도해주세요.",
                List.of()
        );
    }
}
