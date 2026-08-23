package com.platform.notification.controller;

import com.platform.notification.dto.NotificationResponse;
import com.platform.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications(
            Authentication authentication
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                notificationService
                        .getUserNotifications(userId)
        );
    }

    // =========================================================
    // GET SINGLE NOTIFICATION
    // =========================================================

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse>
    getNotification(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                notificationService.getNotification(
                        userId,
                        notificationId
                )
        );
    }
}