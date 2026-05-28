package com.onjeom.backend.domain.ai.api;

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

@Tag(name = "AI Tutor", description = "AI 튜터 API")
public interface AiTutorApi {

    @Operation(
            summary = "AI 튜터 질문",
            description = "학습 중 모르는 내용을 AI 튜터에게 질문합니다. 팀원 AI 서버 연동 완료 시 RAG 기반 답변 제공 예정."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/tutor")
    ResponseEntity<ApiResponse<?>> ask(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TutorQuestionRequest request);
}
