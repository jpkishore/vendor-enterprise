package com.platform.inventory.dto;

import com.platform.inventory.entity.InventoryStatus;

import java.time.Instant;

public record InventoryResponse(

        Long id,

        Long productId,

        Long variantId,

        Integer quantity,

        Integer reservedQuantity,

        Integer availableQuantity,

        Integer reorderLevel,

        InventoryStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}