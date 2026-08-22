package com.platform.payment.repository;

import com.platform.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment>
    findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    Optional<Payment>
    findByOrderId(
            Long orderId
    );

    Optional<Payment>
    findByPaymentNumber(
            String paymentNumber
    );

    Optional<Payment>
    findByTransactionId(
            String transactionId
    );
}