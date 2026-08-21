package com.platform.catalog.dto.product;

import com.platform.catalog.entity.enums.ProductStatus;

import java.time.Instant;

public record ProductResponse(

        Long id,

        Long categoryId,

        String categoryName,

        String name,

        String slug,

        String description,

        String sku,

        ProductStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}