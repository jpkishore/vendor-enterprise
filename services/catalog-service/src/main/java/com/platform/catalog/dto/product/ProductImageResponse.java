package com.platform.catalog.dto.product;

import java.time.Instant;

public record ProductImageResponse(

        Long id,

        Long productId,

        String imageUrl,

        String altText,

        Integer displayOrder,

        Boolean primary,

        Instant createdAt

) {
}