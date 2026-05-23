package com.onjeom.backend.domain.diagnostic.controller;

import com.onjeom.backend.domain.diagnostic.api.DiagnosticApi;
import com.onjeom.backend.domain.diagnostic.dto.request.DiagnosticAnswerRequest;
import com.onjeom.backend.domain.diagnostic.service.DiagnosticService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
public class DiagnosticController implements DiagnosticApi {

    private final DiagnosticService diagnosticService;

    @Override
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<?>> startDiagnostic(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosticService.getFirstProblem(userDetails.getUserId())));
    }

    @Override
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<?>> submitAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DiagnosticAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosticService.submitAnswer(userDetails.getUserId(), request)));
    }

    @Override
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<?>> getLatestResult(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosticService.getLatestResult(userDetails.getUserId())));
    }
}
