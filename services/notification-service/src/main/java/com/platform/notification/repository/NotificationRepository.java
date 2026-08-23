package com.platform.notification.repository;

import com.platform.notification.entity.Notification;
import com.platform.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(
            Long recipientUserId
    );

    List<Notification> findByStatus(
            NotificationStatus status
    );
}