package com.platform.order.dto;

import com.platform.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(

        Long id,

        String orderNumber,

        OrderStatus status,

        BigDecimal totalAmount,

        Instant createdAt

) {
}