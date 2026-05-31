package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.request.TermExplainRequest;
import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.dto.response.TermExplainResponse;
import com.onjeom.backend.domain.ai.dto.response.TutorAnswerResponse;

public interface AiTutorService {

    TutorAnswerResponse ask(Long userId, TutorQuestionRequest request);

    TermExplainResponse explain(TermExplainRequest request);
}
