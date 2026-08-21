package com.platform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Variant ID is required")
        Long variantId,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        @Min(value = 0, message = "Reorder level cannot be negative")
        Integer reorderLevel

) {
}