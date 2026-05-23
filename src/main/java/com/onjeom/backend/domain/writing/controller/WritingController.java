package com.onjeom.backend.domain.writing.controller;

import com.onjeom.backend.domain.writing.api.WritingApi;
import com.onjeom.backend.domain.writing.dto.request.CompareAnswersRequest;
import com.onjeom.backend.domain.writing.dto.request.CurriculumAdjustRequest;
import com.onjeom.backend.domain.writing.dto.request.WeaknessReportRequest;
import com.onjeom.backend.domain.writing.service.WritingAiService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/writing")
@RequiredArgsConstructor
public class WritingController implements WritingApi {

    private final WritingAiService writingAiService;

    @Override
    @PostMapping("/curriculum/adjust")
    public ResponseEntity<ApiResponse<?>> adjustCurriculum(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CurriculumAdjustRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                writingAiService.adjustCurriculum(request)));
    }

    @Override
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<?>> compareAnswers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CompareAnswersRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                writingAiService.compareAnswers(request)));
    }

    @Override
    @PostMapping("/weakness-report")
    public ResponseEntity<ApiResponse<?>> generateWeaknessReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WeaknessReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                writingAiService.generateWeaknessReport(request)));
    }
}
