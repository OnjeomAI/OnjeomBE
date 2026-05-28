package com.onjeom.backend.domain.learning.api;

import com.onjeom.backend.domain.learning.dto.request.HighlightRequest;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Highlight", description = "하이라이트 API")
public interface HighlightApi {

    @Operation(summary = "하이라이트 저장/색상 변경")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    ResponseEntity<ApiResponse<?>> saveHighlight(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody HighlightRequest request);

    @Operation(summary = "문제별 하이라이트 목록 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{problemId}")
    ResponseEntity<ApiResponse<?>> getHighlights(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId);

    @Operation(summary = "하이라이트 삭제")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{problemId}")
    ResponseEntity<ApiResponse<?>> deleteHighlight(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId,
            @Parameter(name = "startOffset", description = "시작 오프셋")
            @RequestParam Integer startOffset,
            @Parameter(name = "endOffset", description = "종료 오프셋")
            @RequestParam Integer endOffset);
}
