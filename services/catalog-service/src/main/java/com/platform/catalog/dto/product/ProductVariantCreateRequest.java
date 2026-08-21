package com.platform.catalog.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductVariantCreateRequest(

        @NotBlank(message = "Variant SKU is required")
        @Size(max = 100)
        String sku,

        @NotBlank(message = "Variant name is required")
        @Size(max = 150)
        String variantName,

        @NotNull(message = "Variant price is required")
        @DecimalMin(
                value = "0.01",
                message = "Variant price must be greater than zero"
        )
        BigDecimal price

) {
}