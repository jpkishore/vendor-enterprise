package com.platform.catalog.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageCreateRequest(

        @NotBlank(message = "Image URL is required")
        @Size(max = 500)
        String imageUrl,

        @Size(max = 255)
        String altText,

        Integer displayOrder,

        Boolean primary

) {
}