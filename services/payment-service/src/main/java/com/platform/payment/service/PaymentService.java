package com.platform.payment.service;

import com.platform.payment.client.OrderClient;
import com.platform.payment.dto.OrderResponse;
import com.platform.payment.dto.PaymentEvent;
import com.platform.payment.dto.PaymentRequest;
import com.platform.payment.dto.PaymentResponse;
import com.platform.payment.entity.Payment;
import com.platform.payment.entity.PaymentStatus;
import com.platform.payment.kafka.PaymentEventProducer;
import com.platform.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderClient orderClient;

    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderClient orderClient,
            PaymentEventProducer paymentEventProducer
    ) {

        this.paymentRepository =
                paymentRepository;

        this.orderClient =
                orderClient;

        this.paymentEventProducer =
                paymentEventProducer;
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    public PaymentResponse createPayment(
            Long userId,
            String idempotencyKey,
            PaymentRequest request
    ) {

        // =====================================================
        // 1. VALIDATE IDEMPOTENCY KEY
        // =====================================================

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        // =====================================================
        // 2. CHECK EXISTING PAYMENT
        // =====================================================

        Payment existingPayment =
                paymentRepository
                        .findByUserIdAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
                        .orElse(null);

        if (existingPayment != null) {

            log.info(
                    "Returning existing payment. userId={}, idempotencyKey={}, paymentId={}",
                    userId,
                    idempotencyKey,
                    existingPayment.getId()
            );

            return toResponse(
                    existingPayment
            );
        }

        // =====================================================
        // 3. GET ORDER
        // =====================================================

        OrderResponse order;

        try {

            order =
                    orderClient.getOrder(
                            request.orderId()
                    );

        } catch (Exception exception) {

            log.error(
                    "Unable to retrieve order. orderId={}",
                    request.orderId(),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to retrieve order",
                    exception
            );
        }

        // =====================================================
        // 4. VALIDATE ORDER
        // =====================================================

        if (order == null) {

            throw new IllegalArgumentException(
                    "Order not found: "
                            + request.orderId()
            );
        }

        if (!order.userId().equals(userId)) {

            throw new IllegalArgumentException(
                    "Order does not belong to current user"
            );
        }

        // =====================================================
        // 5. VALIDATE ORDER STATUS
        // =====================================================

        if (!"CONFIRMED".equals(order.status())) {

            throw new IllegalStateException(
                    "Payment cannot be created for order status: "
                            + order.status()
            );
        }

        // =====================================================
        // 6. CHECK EXISTING ORDER PAYMENT
        // =====================================================

        Payment existingOrderPayment =
                paymentRepository
                        .findByOrderId(
                                order.id()
                        )
                        .orElse(null);

        if (existingOrderPayment != null) {

            return toResponse(
                    existingOrderPayment
            );
        }

        // =====================================================
        // 7. CREATE PAYMENT
        // =====================================================

        Payment payment =
                new Payment();

        payment.setPaymentNumber(
                generatePaymentNumber()
        );

        payment.setOrderId(
                order.id()
        );

        payment.setOrderNumber(
                order.orderNumber()
        );

        payment.setUserId(
                userId
        );

        payment.setAmount(
                order.totalAmount()
        );

        payment.setCurrency(
                "INR"
        );

        payment.setPaymentMethod(
                request.paymentMethod()
        );

        payment.setStatus(
                PaymentStatus.PROCESSING
        );

        payment.setIdempotencyKey(
                idempotencyKey
        );

        // =====================================================
        // 8. SAVE PROCESSING PAYMENT
        // =====================================================

        payment =
                paymentRepository.save(
                        payment
                );

        log.info(
                "Payment created. paymentId={}, paymentNumber={}, orderId={}, amount={}",
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getOrderId(),
                payment.getAmount()
        );

        // =====================================================
        // 9. MOCK PAYMENT PROCESSING
        // =====================================================

        if (Boolean.TRUE.equals(
                request.simulateFailure()
        )) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            payment.setFailureReason(
                    "Payment gateway rejected transaction"
            );

        } else {

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );

            payment.setTransactionId(
                    generateTransactionId()
            );
        }

        payment.setUpdatedAt(
                Instant.now()
        );

        payment =
                paymentRepository.save(
                        payment
                );

        // =====================================================
        // 10. PUBLISH PAYMENT SUCCESS EVENT
        // =====================================================

        String eventType;

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            eventType = "PAYMENT_SUCCESS";
        } else {
            eventType = "PAYMENT_FAILED";
        }

        PaymentEvent event =
                new PaymentEvent(
                        eventType,
                        payment.getId(),
                        payment.getPaymentNumber(),
                        payment.getOrderId(),
                        payment.getOrderNumber(),
                        payment.getUserId(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getPaymentMethod(),
                        payment.getStatus().name(),
                        payment.getTransactionId(),
                        Instant.now()
                );

        paymentEventProducer.publish(event);
        // =====================================================
        // 11. RETURN RESPONSE
        // =====================================================

        return toResponse(
                payment
        );
    }

    // =========================================================
    // GENERATE PAYMENT NUMBER
    // =========================================================

    private String generatePaymentNumber() {

        return "PAY-"
                + Instant.now().toEpochMilli()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    // =========================================================
    // GENERATE TRANSACTION ID
    // =========================================================

    private String generateTransactionId() {

        return "TXN-"
                + UUID.randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                )
                .toUpperCase();
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private PaymentResponse toResponse(
            Payment payment
    ) {

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}