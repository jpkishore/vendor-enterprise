package com.platform.catalog.dto.product;

import jakarta.validation.constraints.*;

public record ProductCreateRequest(

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        String name,

        @NotBlank(message = "Product slug is required")
        @Size(max = 250)
        String slug,

        @Size(max = 2000)
        String description,

        @NotBlank(message = "SKU is required")
        @Size(max = 100)
        String sku

) {
}