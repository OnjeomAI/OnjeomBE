package com.onjeom.backend.domain.curriculum.controller;

import com.onjeom.backend.domain.curriculum.api.CurriculumApi;
import com.onjeom.backend.domain.curriculum.service.CurriculumService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/curriculum")
@RequiredArgsConstructor
public class CurriculumController implements CurriculumApi {

    private final CurriculumService curriculumService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyCurriculum(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getMyCurriculum(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<?>> getProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getProgress(userDetails.getUserId())));
    }

    @Override
    @PatchMapping("/items/{itemId}/start")
    public ResponseEntity<ApiResponse<?>> startItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.startItem(userDetails.getUserId(), itemId)));
    }

    @Override
    @PatchMapping("/items/{itemId}/skip")
    public ResponseEntity<ApiResponse<?>> skipItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.skipItem(userDetails.getUserId(), itemId)));
    }

    @Override
    @PatchMapping("/items/{itemId}/complete")
    public ResponseEntity<ApiResponse<?>> completeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.completeItem(userDetails.getUserId(), itemId)));
    }
}
