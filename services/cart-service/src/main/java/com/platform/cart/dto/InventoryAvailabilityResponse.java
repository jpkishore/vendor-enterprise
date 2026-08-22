package com.platform.cart.dto;

public record InventoryAvailabilityResponse(

        Long id,

        Long productId,

        Long variantId,

        Integer quantity,

        Integer reservedQuantity,

        Integer availableQuantity,

        Integer reorderLevel,

        String status

) {
}