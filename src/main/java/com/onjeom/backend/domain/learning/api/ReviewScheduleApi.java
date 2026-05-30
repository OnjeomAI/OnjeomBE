package com.onjeom.backend.domain.learning.api;

import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "ReviewSchedule", description = "복습 스케줄 API")
public interface ReviewScheduleApi {

    @Operation(summary = "오늘 복습할 문제 목록")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/today")
    ResponseEntity<ApiResponse<?>> getTodayReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "전체 복습 스케줄 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/all")
    ResponseEntity<ApiResponse<?>> getAllSchedules(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
