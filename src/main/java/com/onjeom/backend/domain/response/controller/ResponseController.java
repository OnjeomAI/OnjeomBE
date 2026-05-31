package com.onjeom.backend.domain.response.controller;

import com.onjeom.backend.domain.response.controller.api.ResponseApi;
import com.onjeom.backend.domain.response.dto.request.SubmitAnswerRequest;
import com.onjeom.backend.domain.response.service.ResponseService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class ResponseController implements ResponseApi {

    private final ResponseService responseService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<?>> submit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                responseService.submit(userDetails.getUserId(), request)));
    }

    @Override
    @GetMapping("/{responseId}")
    public ResponseEntity<ApiResponse<?>> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long responseId) {
        return ResponseEntity.ok(ApiResponse.success(
                responseService.getById(userDetails.getUserId(), responseId)));
    }

    @Override
    @GetMapping("/problem/{problemId}")
    public ResponseEntity<ApiResponse<?>> getByProblemId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.success(
                responseService.getByProblemId(userDetails.getUserId(), problemId)));
    }

    @Override
    @GetMapping("/{responseId}/compare")
    public ResponseEntity<ApiResponse<?>> getCompare(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long responseId) {
        return ResponseEntity.ok(ApiResponse.success(
                responseService.getCompare(userDetails.getUserId(), responseId)));
    }
}
