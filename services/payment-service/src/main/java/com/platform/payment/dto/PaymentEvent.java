package com.platform.payment.dto;

import com.platform.payment.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEvent(

        String eventType,

        Long paymentId,

        String paymentNumber,

        Long orderId,

        String orderNumber,

        Long userId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        String status,

        String transactionId,

        Instant occurredAt
) {
}