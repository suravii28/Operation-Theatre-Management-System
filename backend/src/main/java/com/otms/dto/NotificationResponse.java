package com.otms.dto;

import com.otms.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        Long surgeryId,
        LocalDateTime createdAt,
        boolean read
) {
}
