package com.platform.notification.service;

import com.platform.notification.dto.NotificationEvent;
import com.platform.notification.dto.NotificationResponse;
import com.platform.notification.entity.Notification;
import com.platform.notification.entity.NotificationStatus;
import com.platform.notification.entity.NotificationType;
import com.platform.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository =
                notificationRepository;
    }

    // =========================================================
    // PAYMENT SUCCESS
    // =========================================================

    @Transactional
    public void handlePaymentSuccess(
            NotificationEvent event
    ) {

        Notification notification =
                createNotification(
                        event,
                        NotificationType.PAYMENT_SUCCESS,
                        "Payment Successful",
                        "Payment of "
                                + event.amount()
                                + " "
                                + event.currency()
                                + " received successfully for order "
                                + event.orderNumber()
                                + ". Transaction ID: "
                                + event.transactionId()
                );

        sendNotification(notification);
    }

    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    @Transactional
    public void handlePaymentFailed(
            NotificationEvent event
    ) {

        Notification notification =
                createNotification(
                        event,
                        NotificationType.PAYMENT_FAILED,
                        "Payment Failed",
                        "Payment failed for order "
                                + event.orderNumber()
                                + ". Amount: "
                                + event.amount()
                                + " "
                                + event.currency()
                );

        sendNotification(notification);
    }

    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    private Notification createNotification(
            NotificationEvent event,
            NotificationType type,
            String subject,
            String message
    ) {

        Notification notification =
                new Notification();

        notification.setNotificationType(type);

        notification.setRecipientUserId(
                event.userId()
        );

        notification.setOrderId(
                event.orderId()
        );

        notification.setOrderNumber(
                event.orderNumber()
        );

        notification.setPaymentId(
                event.paymentId()
        );

        notification.setSubject(
                subject
        );

        notification.setMessage(
                message
        );

        notification.setStatus(
                NotificationStatus.PENDING
        );

        notification.setRetryCount(0);

        return notificationRepository.save(
                notification
        );
    }

    // =========================================================
    // SEND NOTIFICATION
    // =========================================================

    private void sendNotification(
            Notification notification
    ) {

        try {

            log.info(
                    "Sending notification. id={}, type={}, userId={}",
                    notification.getId(),
                    notification.getNotificationType(),
                    notification.getRecipientUserId()
            );

            /*
             * For now this is a mock notification sender.
             *
             * Later we will connect:
             *
             * Email → SMTP / AWS SES / SendGrid
             * SMS   → Twilio / AWS SNS
             * Push  → Firebase
             */

            log.info(
                    """
                            ==============================
                            NOTIFICATION
                            ==============================
                            ID       : {}
                            Type     : {}
                            User ID  : {}
                            Order    : {}
                            Subject  : {}
                            Message  : {}
                            ==============================
                            """,
                    notification.getId(),
                    notification.getNotificationType(),
                    notification.getRecipientUserId(),
                    notification.getOrderNumber(),
                    notification.getSubject(),
                    notification.getMessage()
            );

            notification.setStatus(
                    NotificationStatus.SENT
            );

            notification.setSentAt(
                    Instant.now()
            );

            notification.setFailureReason(
                    null
            );

            notificationRepository.save(
                    notification
            );

            log.info(
                    "Notification sent successfully. id={}",
                    notification.getId()
            );

        } catch (Exception exception) {

            markFailed(
                    notification,
                    exception
            );
        }
    }

    // =========================================================
    // MARK FAILED
    // =========================================================

    private void markFailed(
            Notification notification,
            Exception exception
    ) {

        int retryCount =
                notification.getRetryCount() == null
                        ? 0
                        : notification.getRetryCount();

        notification.setRetryCount(
                retryCount + 1
        );

        notification.setFailureReason(
                exception.getMessage()
        );

        notification.setStatus(
                NotificationStatus.FAILED
        );

        notificationRepository.save(
                notification
        );

        log.error(
                "Notification failed. id={}, retryCount={}",
                notification.getId(),
                notification.getRetryCount(),
                exception
        );
    }

    @Transactional
    public void retryNotification(
            Notification notification
    ) {

        if (notification.getStatus()
                != NotificationStatus.FAILED) {

            return;
        }

        if (notification.getRetryCount()
                >= maxRetryAttempts) {

            log.warn(
                    "Maximum retry attempts reached. notificationId={}, retryCount={}",
                    notification.getId(),
                    notification.getRetryCount()
            );

            return;
        }

        log.info(
                "Retrying notification. notificationId={}, attempt={}",
                notification.getId(),
                notification.getRetryCount() + 1
        );

        try {

            // =========================================
            // MOCK SEND
            // =========================================

            log.info(
                    "Retry notification sent. notificationId={}",
                    notification.getId()
            );

            notification.setStatus(
                    NotificationStatus.SENT
            );

            notification.setSentAt(
                    Instant.now()
            );

            notification.setFailureReason(
                    null
            );

            notificationRepository.save(
                    notification
            );

        } catch (Exception exception) {

            int retryCount =
                    notification.getRetryCount() + 1;

            notification.setRetryCount(
                    retryCount
            );

            notification.setFailureReason(
                    exception.getMessage()
            );

            notificationRepository.save(
                    notification
            );
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(
            Long userId
    ) {

        return notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(
            Long userId,
            Long notificationId
    ) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .filter(n ->
                                n.getRecipientUserId()
                                        .equals(userId)
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found: "
                                                + notificationId
                                )
                        );

        return toResponse(notification);
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {

        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getRecipientUserId(),
                notification.getOrderId(),
                notification.getOrderNumber(),
                notification.getPaymentId(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getRetryCount(),
                notification.getFailureReason(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }


}