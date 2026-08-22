package com.platform.order.dto;

public record CartItemResponse(

        Long id,

        Long productId,

        Long variantId,

        Integer quantity

) {
}