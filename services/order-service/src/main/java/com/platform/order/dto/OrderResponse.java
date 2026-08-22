package com.platform.order.dto;

import com.platform.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long userId,

        OrderStatus status,

        BigDecimal totalAmount,

        List<OrderItemResponse> items,

        Instant createdAt,

        Instant updatedAt

) {
}