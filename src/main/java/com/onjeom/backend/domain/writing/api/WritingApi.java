package com.onjeom.backend.domain.writing.api;

import com.onjeom.backend.domain.writing.dto.request.CompareAnswersRequest;
import com.onjeom.backend.domain.writing.dto.request.CurriculumAdjustRequest;
import com.onjeom.backend.domain.writing.dto.request.WeaknessReportRequest;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Writing AI", description = "글쓰기 AI 분석 API")
public interface WritingApi {

    @Operation(
            summary = "동적 학습 경로 재조정",
            description = "역량별 점수 이력을 분석하여 3회 연속 50점 미만인 취약 역량을 탐지하고 커리큘럼 재조정 메시지를 생성합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/curriculum/adjust")
    ResponseEntity<ApiResponse<?>> adjustCurriculum(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CurriculumAdjustRequest request);

    @Operation(
            summary = "답변 변화 추적",
            description = "이전 답변과 현재 답변을 비교하여 성장 메시지와 새로 포함된/누락된 키워드를 반환합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/compare")
    ResponseEntity<ApiResponse<?>> compareAnswers(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CompareAnswersRequest request);

    @Operation(
            summary = "약점 분석 리포트 생성",
            description = "역량별 평균 점수를 분석하여 취약/보통 수준을 분류하고 LLM 기반 약점 리포트와 개선 권장사항을 생성합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/weakness-report")
    ResponseEntity<ApiResponse<?>> generateWeaknessReport(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody WeaknessReportRequest request);
}
