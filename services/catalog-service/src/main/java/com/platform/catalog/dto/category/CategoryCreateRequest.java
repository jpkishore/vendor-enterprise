package com.platform.catalog.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 150, message = "Category name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Category slug is required")
        @Size(max = 180, message = "Category slug cannot exceed 180 characters")
        String slug,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description

) {
}