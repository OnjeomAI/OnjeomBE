package com.onjeom.backend.domain.curriculum.api;

import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Curriculum", description = "커리큘럼 API")
public interface CurriculumApi {

    @Operation(summary = "내 현재 커리큘럼 및 오늘 학습 문제 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    ResponseEntity<ApiResponse<?>> getMyCurriculum(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "커리큘럼 진행률 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/progress")
    ResponseEntity<ApiResponse<?>> getProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "커리큘럼 아이템 학습 시작")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/items/{itemId}/start")
    ResponseEntity<ApiResponse<?>> startItem(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId);

    @Operation(summary = "커리큘럼 아이템 스킵")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/items/{itemId}/skip")
    ResponseEntity<ApiResponse<?>> skipItem(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId);

    @Operation(summary = "커리큘럼 아이템 완료 처리")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/items/{itemId}/complete")
    ResponseEntity<ApiResponse<?>> completeItem(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId);
}
