package com.platform.notification.kafka;

import com.platform.notification.dto.NotificationEvent;
import com.platform.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventConsumer {
    private final NotificationService notificationService;
    public NotificationEventConsumer(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    // =========================================================
    // PAYMENT EVENTS
    // =========================================================

    @KafkaListener(
            topics = "${app.kafka.topics.payment}",
            groupId = "notification-service-payment-consumer"
    )
    public void consumePaymentEvent(
            NotificationEvent event
    ) {

        log.info(
                "Payment event received. eventType={}, orderId={}, paymentId={}",
                event.eventType(),
                event.orderId(),
                event.paymentId()
        );

        switch (event.eventType()) {

            case "PAYMENT_SUCCESS" ->

                    notificationService
                            .handlePaymentSuccess(
                                    event
                            );

            case "PAYMENT_FAILED" ->

                    notificationService
                            .handlePaymentFailed(
                                    event
                            );

            default ->

                    log.warn(
                            "Unknown payment event type={}",
                            event.eventType()
                    );
        }
    }
    // =========================================================
    // PAYMENT SUCCESS
    // =========================================================

    private void sendPaymentSuccessNotification(
            NotificationEvent event
    ) {

        log.info(
                """
                PAYMENT SUCCESS NOTIFICATION

                Order Number : {}
                Payment Number : {}
                Amount : {}
                Transaction ID : {}
                User ID : {}
                """,
                event.orderNumber(),
                event.paymentNumber(),
                event.amount(),
                event.transactionId(),
                event.userId()
        );

        // TODO:
        // Email Service
        // SMS Service
        // Push Notification
    }

    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    private void sendPaymentFailedNotification(
            NotificationEvent event
    ) {

        log.info(
                """
                PAYMENT FAILED NOTIFICATION

                Order Number : {}
                Payment Number : {}
                Amount : {}
                User ID : {}
                """,
                event.orderNumber(),
                event.paymentNumber(),
                event.amount(),
                event.userId()
        );

        // TODO:
        // Email Service
        // SMS Service
        // Push Notification
    }
}