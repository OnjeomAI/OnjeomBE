package com.onjeom.backend.domain.notification.controller;

import com.onjeom.backend.domain.notification.api.NotificationApi;
import com.onjeom.backend.domain.notification.service.NotificationService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<?>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(userDetails.getUserId())));
    }
}
