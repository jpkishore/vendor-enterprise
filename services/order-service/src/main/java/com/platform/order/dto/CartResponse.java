package com.platform.order.dto;

import java.time.Instant;
import java.util.List;

public record CartResponse(

        Long id,

        Long userId,

        String status,

        List<CartItemResponse> items,

        Instant createdAt,

        Instant updatedAt

) {
}