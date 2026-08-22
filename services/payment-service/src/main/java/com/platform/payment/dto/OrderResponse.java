package com.platform.payment.dto;

import com.platform.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long userId,

        String status,

        BigDecimal totalAmount,

        List<OrderItemResponse> items,

        Instant createdAt,

        Instant updatedAt
) {
}