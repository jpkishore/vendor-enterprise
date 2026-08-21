package com.platform.catalog.dto.product;

import com.platform.catalog.entity.enums.ProductVariantStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductVariantResponse(

        Long id,

        Long productId,

        String sku,

        String variantName,

        BigDecimal price,

        ProductVariantStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}