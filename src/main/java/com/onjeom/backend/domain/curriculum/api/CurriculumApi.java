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
}
