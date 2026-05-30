package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.dto.response.TutorAnswerResponse;

public interface AiTutorService {

    TutorAnswerResponse ask(Long userId, TutorQuestionRequest request);
}
