package com.onjeom.backend.domain.dashboard.controller;

import com.onjeom.backend.domain.dashboard.api.DashboardApi;
import com.onjeom.backend.domain.dashboard.service.DashboardService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    private final DashboardService dashboardService;

    @Override
    @GetMapping("/dashboard/radar")
    public ResponseEntity<ApiResponse<?>> getRadarChart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getRadarChart(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getLearningStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getLearningStats(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/dashboard/today")
    public ResponseEntity<ApiResponse<?>> getTodayGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getTodayGoal(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/dashboard/recent-responses")
    public ResponseEntity<ApiResponse<?>> getRecentResponses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getRecentResponses(userDetails.getUserId(), page, size)));
    }

    @Override
    @GetMapping("/dashboard/weak-points")
    public ResponseEntity<ApiResponse<?>> getWeakPointReport(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getWeakPointReport(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/admin/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getAdminStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getAdminStats()));
    }

    @Override
    @GetMapping(value = "/admin/dashboard/stats/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportAdminStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String csv = dashboardService.exportAdminStatsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"onjeom-stats.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
