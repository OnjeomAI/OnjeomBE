package com.onjeom.backend.domain.learning.controller;

import com.onjeom.backend.domain.learning.api.HighlightApi;
import com.onjeom.backend.domain.learning.dto.request.HighlightRequest;
import com.onjeom.backend.domain.learning.service.HighlightService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/highlights")
@RequiredArgsConstructor
public class HighlightController implements HighlightApi {

    private final HighlightService highlightService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<?>> saveHighlight(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody HighlightRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                highlightService.saveHighlight(userDetails.getUserId(), request)));
    }

    @Override
    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<?>> getHighlights(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.success(
                highlightService.getHighlights(userDetails.getUserId(), problemId)));
    }

    @Override
    @DeleteMapping("/{problemId}")
    public ResponseEntity<ApiResponse<?>> deleteHighlight(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long problemId,
            @RequestParam Integer startOffset,
            @RequestParam Integer endOffset) {
        highlightService.deleteHighlight(userDetails.getUserId(), problemId, startOffset, endOffset);
        return ResponseEntity.ok(ApiResponse.success("하이라이트가 삭제되었습니다.", null));
    }
}
