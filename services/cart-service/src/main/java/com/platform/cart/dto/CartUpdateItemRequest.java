package com.platform.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartUpdateItemRequest(

        @NotNull
        @Min(1)
        Integer quantity

) {
}