package com.onjeom.backend.domain.dashboard.api;

import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;

@Tag(name = "Dashboard", description = "대시보드 API")
public interface DashboardApi {

    @Operation(summary = "역량 레이더 차트 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/dashboard/radar")
    ResponseEntity<ApiResponse<?>> getRadarChart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "학습 통계 조회 (최근 7일)")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/dashboard/stats")
    ResponseEntity<ApiResponse<?>> getLearningStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "오늘의 학습 목표 및 복습 현황 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/dashboard/today")
    ResponseEntity<ApiResponse<?>> getTodayGoal(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "최근 답변 이력 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/dashboard/recent-responses")
    ResponseEntity<ApiResponse<?>> getRecentResponses(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(summary = "약점 리포트 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/dashboard/weak-points")
    ResponseEntity<ApiResponse<?>> getWeakPointReport(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "관리자 전체 통계 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/admin/dashboard/stats")
    ResponseEntity<ApiResponse<?>> getAdminStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "관리자 통계 CSV 내보내기", description = "전체 사용자 통계 및 역량별 평균 점수를 CSV 파일로 다운로드합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping(value = "/admin/dashboard/stats/export", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> exportAdminStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
