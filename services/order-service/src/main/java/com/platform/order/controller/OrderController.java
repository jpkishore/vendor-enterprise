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
            Authentication authentication
    ) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderService.createOrder(userId)
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
            Authentication authentication,
            @PathVariable Long orderId
    ) {

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