package com.platform.catalog.dto.category;

import com.platform.catalog.entity.enums.CategoryStatus;

import java.time.Instant;

public record CategoryResponse(

        Long id,

        String name,

        String slug,

        String description,

        CategoryStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}