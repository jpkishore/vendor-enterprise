package com.platform.order.dto;

public record InventoryResponse(

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