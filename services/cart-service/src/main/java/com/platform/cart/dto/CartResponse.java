package com.platform.cart.dto;

import com.platform.cart.entity.CartStatus;

import java.time.Instant;
import java.util.List;

public record CartResponse(

        Long id,

        Long userId,

        CartStatus status,

        List<CartItemResponse> items,

        Instant createdAt,

        Instant updatedAt

) {
}