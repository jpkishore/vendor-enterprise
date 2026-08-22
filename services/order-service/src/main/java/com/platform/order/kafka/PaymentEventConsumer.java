package com.platform.order.kafka;

import com.platform.order.client.InventoryClient;
import com.platform.order.dto.PaymentEvent;
import com.platform.order.dto.StockRequest;
import com.platform.order.entity.Order;
import com.platform.order.entity.OrderItem;
import com.platform.order.entity.OrderStatus;
import com.platform.order.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public PaymentEventConsumer(
            OrderRepository orderRepository, InventoryClient inventoryClient
    ) {

        this.orderRepository =
                orderRepository;
        this.inventoryClient = inventoryClient;
    }

    // =========================================================
    // PAYMENT EVENT CONSUMER
    // =========================================================

    @KafkaListener(
            topics = "${app.kafka.topics.payment}",
            groupId = "order-service-payment-consumer"
    )
    @Transactional
    public void consume(
            PaymentEvent event
    ) {

        log.info(
                "Payment event received. eventType={}, paymentId={}, orderId={}, status={}",
                event.eventType(),
                event.paymentId(),
                event.orderId(),
                event.status()
        );

        // =====================================================
        // VALIDATE EVENT
        // =====================================================

        if (event.orderId() == null) {

            log.error(
                    "Payment event does not contain orderId"
            );

            return;
        }

        // =====================================================
        // FIND ORDER
        // =====================================================

        Order order =
                orderRepository
                        .findById(
                                event.orderId()
                        )
                        .orElse(null);

        if (order == null) {

            log.error(
                    "Order not found for payment event. orderId={}",
                    event.orderId()
            );

            return;
        }

        // =====================================================
        // PAYMENT SUCCESS
        // =====================================================

        if ("PAYMENT_SUCCESS".equals(
                event.eventType()
        )) {

            handlePaymentSuccess(
                    order,
                    event
            );

            return;
        }

        // =====================================================
        // PAYMENT FAILED
        // =====================================================

        if ("PAYMENT_FAILED".equals(
                event.eventType()
        )) {

            handlePaymentFailed(
                    order,
                    event
            );

            return;
        }

        log.warn(
                "Unknown payment event type: {}",
                event.eventType()
        );
    }

    // =========================================================
    // PAYMENT SUCCESS
    // =========================================================

    private void handlePaymentSuccess(
            Order order,
            PaymentEvent event
    ) {

        // Idempotency:
        // If Kafka delivers the same event again,
        // don't perform unnecessary updates.

        if (order.getStatus() == OrderStatus.PAID) {

            log.info(
                    "Order already marked PAID. orderId={}",
                    order.getId()
            );

            return;
        }

        // =====================================================
        // VALIDATE PAYMENT
        // =====================================================

        if (!"SUCCESS".equals(
                event.status()
        )) {

            log.warn(
                    "PAYMENT_SUCCESS event has unexpected status. orderId={}, status={}",
                    order.getId(),
                    event.status()
            );

            return;
        }

        // =====================================================
        // VALIDATE AMOUNT
        // =====================================================

        if (event.amount() == null
                || order.getTotalAmount() == null
                || order.getTotalAmount()
                .compareTo(event.amount()) != 0) {

            log.error(
                    "Payment amount mismatch. orderId={}, orderAmount={}, paymentAmount={}",
                    order.getId(),
                    order.getTotalAmount(),
                    event.amount()
            );

            throw new IllegalStateException(
                    "Payment amount does not match order amount"
            );
        }

        // =====================================================
        // UPDATE ORDER
        // =====================================================

        order.setStatus(
                OrderStatus.PAID
        );

        orderRepository.save(
                order
        );

        log.info(
                "Order marked as PAID. orderId={}, orderNumber={}, paymentId={}",
                order.getId(),
                order.getOrderNumber(),
                event.paymentId()
        );
    }

    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    private void handlePaymentFailed(
            Order order,
            PaymentEvent event
    ) {

        if (order.getStatus() ==
                OrderStatus.PAYMENT_FAILED) {

            log.info(
                    "Order already marked PAYMENT_FAILED. orderId={}",
                    order.getId()
            );

            return;
        }

        // =========================================
        // RELEASE INVENTORY
        // =========================================

        for (OrderItem item :
                order.getItems()) {

            try {

                inventoryClient.release(
                        item.getVariantId(),
                        new StockRequest(
                                item.getQuantity()
                        )
                );

                log.info(
                        "Inventory released after payment failure. " +
                                "orderId={}, variantId={}, quantity={}",
                        order.getId(),
                        item.getVariantId(),
                        item.getQuantity()
                );

            } catch (Exception exception) {

                log.error(
                        "Failed to release inventory after payment failure. " +
                                "orderId={}, variantId={}",
                        order.getId(),
                        item.getVariantId(),
                        exception
                );

                throw new IllegalStateException(
                        "Unable to release inventory",
                        exception
                );
            }
        }

        // =========================================
        // UPDATE ORDER
        // =========================================

        order.setStatus(
                OrderStatus.PAYMENT_FAILED
        );

        orderRepository.save(
                order
        );

        log.info(
                "Order marked PAYMENT_FAILED. orderId={}, paymentId={}",
                order.getId(),
                event.paymentId()
        );
    }
}