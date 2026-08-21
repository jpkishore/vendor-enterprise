package com.platform.inventory.dto;

import com.platform.inventory.kafka.InventoryEventType;

import java.time.Instant;

public record InventoryEvent(

        InventoryEventType eventType,

        Long inventoryId,

        Long productId,

        Long variantId,

        Integer quantity,

        Integer reservedQuantity,

        Integer availableQuantity,

        Instant occurredAt

) {
}