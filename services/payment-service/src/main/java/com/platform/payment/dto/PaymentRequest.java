package com.platform.payment.dto;

import com.platform.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull
        Long orderId,

        @NotNull
        PaymentMethod paymentMethod,

        Boolean simulateFailure
) {
}