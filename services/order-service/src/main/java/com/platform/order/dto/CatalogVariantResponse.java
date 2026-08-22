package com.platform.order.dto;

import java.math.BigDecimal;

public record CatalogVariantResponse(
        Long id,
        Long productId,
        BigDecimal price
) {
}