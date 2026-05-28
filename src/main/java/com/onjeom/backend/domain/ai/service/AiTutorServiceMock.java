package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.dto.response.TutorAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class AiTutorServiceMock implements AiTutorService {

    @Override
    public TutorAnswerResponse ask(Long userId, TutorQuestionRequest request) {
        // TODO: 팀원 AI 서버 /api/tutor/ask 엔드포인트 완성 시
        //        AiTutorServiceImpl로 교체 예정
        //        AiScoringServiceImpl과 동일한 RestClient 방식으로 구현
        log.info("[MOCK] AI 튜터 호출 생략 - question: {}", request.question());
        return new TutorAnswerResponse(
                "AI 튜터가 준비 중입니다. 잠시 후 다시 시도해주세요.",
                List.of()
        );
    }
}
