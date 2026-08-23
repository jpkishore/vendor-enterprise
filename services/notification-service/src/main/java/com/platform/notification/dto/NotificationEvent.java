package com.platform.notification.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record NotificationEvent(

        String eventType,

        Long paymentId,

        String paymentNumber,

        Long orderId,

        String orderNumber,

        Long userId,

        BigDecimal amount,

        String currency,

        String paymentMethod,

        String status,

        String transactionId,

        Instant occurredAt
) {
}