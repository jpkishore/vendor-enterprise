package com.platform.cart.dto;

public record CartItemResponse(

        Long id,

        Long productId,

        Long variantId,

        Integer quantity

) {
}