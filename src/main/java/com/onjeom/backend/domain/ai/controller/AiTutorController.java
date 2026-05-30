package com.onjeom.backend.domain.ai.controller;

import com.onjeom.backend.domain.ai.api.AiTutorApi;
import com.onjeom.backend.domain.ai.dto.request.TermExplainRequest;
import com.onjeom.backend.domain.ai.dto.request.TutorQuestionRequest;
import com.onjeom.backend.domain.ai.service.AiTutorService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTutorController implements AiTutorApi {

    private final AiTutorService aiTutorService;

    @Override
    @PostMapping("/tutor")
    public ResponseEntity<ApiResponse<?>> ask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TutorQuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                aiTutorService.ask(userDetails.getUserId(), request)));
    }

    @Override
    @PostMapping("/explain")
    public ResponseEntity<ApiResponse<?>> explain(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TermExplainRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                aiTutorService.explain(request)));
    }
}
