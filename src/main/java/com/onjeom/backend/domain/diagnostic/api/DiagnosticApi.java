package com.onjeom.backend.domain.diagnostic.api;

import com.onjeom.backend.domain.diagnostic.dto.request.DiagnosticAnswerRequest;
import com.onjeom.backend.domain.diagnostic.dto.request.DiagnosticStartRequest;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Diagnostic", description = "진단 테스트 API")
public interface DiagnosticApi {

    @Operation(summary = "진단 테스트 첫 문항 요청")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/start")
    ResponseEntity<ApiResponse<?>> startDiagnostic(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "답변 제출 및 다음 문항 또는 결과 반환")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/submit")
    ResponseEntity<ApiResponse<?>> submitAnswer(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DiagnosticAnswerRequest request);

    @Operation(summary = "최근 진단 결과 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/result")
    ResponseEntity<ApiResponse<?>> getLatestResult(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
