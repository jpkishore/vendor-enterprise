package com.platform.notification.scheduler;

import com.platform.notification.entity.Notification;
import com.platform.notification.entity.NotificationStatus;
import com.platform.notification.repository.NotificationRepository;
import com.platform.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NotificationRetryScheduler {

    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;

    public NotificationRetryScheduler(
            NotificationRepository notificationRepository,
            NotificationService notificationService
    ) {

        this.notificationRepository =
                notificationRepository;

        this.notificationService =
                notificationService;
    }

    @Scheduled(
            fixedDelayString = "${app.notification.retry.interval:60000}"
    )
    public void retryFailedNotifications() {

        log.info(
                "Checking failed notifications for retry..."
        );

        List<Notification> notifications =
                notificationRepository.findByStatus(
                        NotificationStatus.FAILED
                );

        if (notifications.isEmpty()) {

            log.info(
                    "No failed notifications found."
            );

            return;
        }

        log.info(
                "Found {} failed notifications.",
                notifications.size()
        );

        for (Notification notification :
                notifications) {

            try {

                notificationService.retryNotification(
                        notification
                );

            } catch (Exception exception) {

                log.error(
                        "Retry processing failed. notificationId={}",
                        notification.getId(),
                        exception
                );
            }
        }
    }
}