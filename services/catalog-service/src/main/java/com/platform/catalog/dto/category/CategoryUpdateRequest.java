package com.platform.catalog.dto.category;

import com.platform.catalog.entity.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Category slug is required")
        @Size(max = 180)
        String slug,

        @Size(max = 500)
        String description,

        CategoryStatus status

) {
}