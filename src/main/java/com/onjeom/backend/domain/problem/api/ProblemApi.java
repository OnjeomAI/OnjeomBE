package com.onjeom.backend.domain.problem.api;

import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Problem", description = "문제 API")
public interface ProblemApi {

    @Operation(summary = "문제 목록 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping
    ResponseEntity<ApiResponse<?>> getProblems(
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(name = "size", description = "페이지당 문제 수", example = "20")
            @RequestParam(defaultValue = "20") int size);

    @Operation(summary = "문제 상세 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{problemId}")
    ResponseEntity<ApiResponse<?>> getProblemDetail(@PathVariable Long problemId);

    @Operation(summary = "독해 유형별 문제 조회", description = "조회 가능 유형: FACTUAL·INFERENTIAL·CRITICAL·CREATIVE (VOCABULARY·LOGICAL은 역량 축으로만 사용)")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/type/{readingType}")
    ResponseEntity<ApiResponse<?>> getProblemsByReadingType(
            @Parameter(description = "독해 유형", schema = @Schema(allowableValues = {"FACTUAL", "INFERENTIAL", "CRITICAL", "CREATIVE"}))
            @PathVariable ReadingType readingType);
}
