package com.platform.order.controller;

import com.platform.order.dto.OrderResponse;
import com.platform.order.dto.OrderSummaryResponse;
import com.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================================================
    // CREATE ORDER
    // =========================================================


    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderService.createOrder(
                                userId,
                                idempotencyKey
                        )
                );
    }
    // =========================================================
    // GET MY ORDERS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(
            Authentication authentication
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                orderService.getMyOrders(userId)
        );
    }

    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        boolean internalService =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_INTERNAL_SERVICE")
                        );

        // Payment Service / internal service
        if (internalService) {

            return ResponseEntity.ok(
                    orderService.getOrderById(
                            orderId
                    )
            );
        }

        // Customer JWT
        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                orderService.getOrder(
                        userId,
                        orderId
                )
        );
    }

    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        userId,
                        orderId
                )
        );
    }
}