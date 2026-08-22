package com.platform.order.dto;

import java.math.BigDecimal;

public record OrderEventItem(

        Long productId,

        Long variantId,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice

) {
}