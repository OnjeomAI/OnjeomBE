package com.onjeom.backend.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationResponse(
        List<NotificationItem> notifications
) {
    public record NotificationItem(
            String type,
            String title,
            String message,
            LocalDateTime createdAt
    ) {}
}
