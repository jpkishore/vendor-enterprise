package com.platform.payment.dto;

import com.platform.payment.entity.PaymentMethod;
import com.platform.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(

        Long id,

        String paymentNumber,

        Long orderId,

        String orderNumber,

        Long userId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        PaymentStatus status,

        String transactionId,

        String failureReason,

        Instant createdAt,

        Instant updatedAt
) {
}