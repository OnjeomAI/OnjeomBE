package com.onjeom.backend.domain.notification.api;

import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

    @Operation(summary = "내 알림 목록 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/notifications")
    ResponseEntity<ApiResponse<?>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
