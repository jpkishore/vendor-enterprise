package com.platform.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderEvent(

        Long orderId,

        String orderNumber,

        Long userId,

        String status,

        BigDecimal totalAmount,

        List<OrderEventItem> items,

        Instant occurredAt

) {
}