package com.onjeom.backend.domain.cms.api;

import com.onjeom.backend.domain.cms.dto.request.GenerateProblemRequest;
import com.onjeom.backend.domain.cms.dto.request.UpdateKeywordRequest;
import com.onjeom.backend.domain.problem.dto.request.CreateProblemRequest;
import com.onjeom.backend.domain.problem.dto.request.UpdateProblemRequest;
import com.onjeom.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CMS", description = "관리자 콘텐츠 관리 API")
public interface CmsApi {

    @Operation(summary = "전체 문제 목록 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/problems")
    ResponseEntity<ApiResponse<?>> getAllProblems(
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(name = "size", description = "페이지당 문제 수", example = "20")
            @RequestParam(defaultValue = "20") int size);

    @Operation(summary = "문제 등록")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/problems")
    ResponseEntity<ApiResponse<?>> createProblem(@Valid @RequestBody CreateProblemRequest request);

    @Operation(summary = "AI 문제 자동 생성", description = "AI가 난이도와 독해 유형에 맞는 지문/문제/모범답안을 생성하여 DB에 저장합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/problems/generate")
    ResponseEntity<ApiResponse<?>> generateProblem(@Valid @RequestBody GenerateProblemRequest request);

    @Operation(summary = "문제 수정 (부분 업데이트)")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/problems/{problemId}")
    ResponseEntity<ApiResponse<?>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody UpdateProblemRequest request);

    @Operation(summary = "문제 삭제")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/problems/{problemId}")
    ResponseEntity<ApiResponse<?>> deleteProblem(@PathVariable Long problemId);

    @Operation(summary = "핵심 키워드 등록/수정")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/problems/{problemId}/keywords")
    ResponseEntity<ApiResponse<?>> updateKeywords(
            @PathVariable Long problemId,
            @Valid @RequestBody UpdateKeywordRequest request);
}
