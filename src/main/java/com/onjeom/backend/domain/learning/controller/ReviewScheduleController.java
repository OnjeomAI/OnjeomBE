package com.onjeom.backend.domain.learning.controller;

import com.onjeom.backend.domain.learning.api.ReviewScheduleApi;
import com.onjeom.backend.domain.learning.service.ReviewScheduleService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewScheduleController implements ReviewScheduleApi {

    private final ReviewScheduleService reviewScheduleService;

    @Override
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<?>> getTodayReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewScheduleService.getTodayReviews(userDetails.getUserId())));
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewScheduleService.getAllSchedules(userDetails.getUserId())));
    }
}
