package com.onjeom.backend.domain.ai.api;

import com.onjeom.backend.domain.ai.dto.request.TermExplainRequest;
import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@Tag(name = "AI Tutor", description = "AI 튜터 API")
public interface AiTutorApi {

    @Operation(
            summary = "AI 튜터 질문",
            description = "학습 중 모르는 내용을 AI 튜터에게 질문합니다. RAG 기반 답변 제공."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/tutor")
    ResponseEntity<ApiResponse<?>> ask(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TutorQuestionRequest request);

    @Operation(
            summary = "용어 설명 (도움 기능)",
            description = "이해가 안 되는 용어나 문장을 입력하면 AI가 쉬운 말로 설명합니다. passageText 제공 시 지문 맥락 기반으로 설명합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/explain")
    ResponseEntity<ApiResponse<?>> explain(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TermExplainRequest request);
}
