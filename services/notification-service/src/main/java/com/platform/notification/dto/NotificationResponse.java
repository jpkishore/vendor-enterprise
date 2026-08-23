package com.platform.notification.dto;

import com.platform.notification.entity.NotificationStatus;
import com.platform.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType notificationType,
        Long recipientUserId,
        Long orderId,
        String orderNumber,
        Long paymentId,
        String subject,
        String message,
        NotificationStatus status,
        Integer retryCount,
        String failureReason,
        Instant createdAt,
        Instant sentAt
) {
}