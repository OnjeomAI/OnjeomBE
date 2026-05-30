package com.onjeom.backend.domain.response.controller.api;

import com.onjeom.backend.domain.response.dto.request.SubmitAnswerRequest;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Response", description = "답변 채점 API")
@SecurityRequirement(name = "BearerAuth")
public interface ResponseApi {

    @Operation(summary = "답변 제출 및 AI 채점")
    @PostMapping
    ResponseEntity<ApiResponse<?>> submit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmitAnswerRequest request);

    @Operation(summary = "채점 결과 상세 조회")
    @GetMapping("/{responseId}")
    ResponseEntity<ApiResponse<?>> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long responseId);

    @Operation(summary = "문제별 내 답변 이력 조회")
    @GetMapping("/problem/{problemId}")
    ResponseEntity<ApiResponse<?>> getByProblemId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId);
}
