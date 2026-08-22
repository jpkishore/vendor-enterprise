package com.platform.order.service;

import com.platform.order.client.CartClient;
import com.platform.order.client.CatalogClient;
import com.platform.order.client.InventoryClient;
import com.platform.order.dto.*;
import com.platform.order.entity.Order;
import com.platform.order.entity.OrderItem;
import com.platform.order.entity.OrderStatus;
import com.platform.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    public OrderService(
            OrderRepository orderRepository,
            CartClient cartClient,
            CatalogClient catalogClient, InventoryClient inventoryClient
    ) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    public OrderResponse createOrder(Long userId) {

        // =========================================================
        // 1. GET CUSTOMER CART
        // =========================================================

        CartResponse cart;

        try {

            cart = cartClient.getCart();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to retrieve cart",
                    exception
            );
        }

        // =========================================================
        // 2. VALIDATE CART
        // =========================================================

        if (cart == null) {

            throw new IllegalStateException(
                    "Cart not found"
            );
        }

        if (!cart.userId().equals(userId)) {

            throw new IllegalStateException(
                    "Cart does not belong to current user"
            );
        }

        if (cart.items() == null
                || cart.items().isEmpty()) {

            throw new IllegalStateException(
                    "Cannot create order from empty cart"
            );
        }

        // =========================================================
        // 3. CREATE PENDING ORDER
        // =========================================================

        Order order = new Order();

        order.setOrderNumber(
                generateOrderNumber()
        );

        order.setUserId(userId);

        order.setStatus(
                OrderStatus.PENDING
        );

        order.setTotalAmount(
                BigDecimal.ZERO
        );

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        // =========================================================
        // 4. PROCESS CART ITEMS
        // =========================================================

        for (CartItemResponse cartItem :
                cart.items()) {

            // -----------------------------------------------------
            // Validate Product
            // -----------------------------------------------------

            CatalogProductResponse product;

            try {

                product =
                        catalogClient.getProduct(
                                cartItem.productId()
                        );

            } catch (Exception exception) {

                throw new IllegalStateException(
                        "Unable to validate product: "
                                + cartItem.productId(),
                        exception
                );
            }

            if (product == null) {

                throw new IllegalArgumentException(
                        "Product not found: "
                                + cartItem.productId()
                );
            }

            // -----------------------------------------------------
            // Validate Variant
            // -----------------------------------------------------

            CatalogVariantResponse variant;

            try {

                variant =
                        catalogClient.getVariant(
                                cartItem.productId(),
                                cartItem.variantId()
                        );

            } catch (Exception exception) {

                throw new IllegalStateException(
                        "Unable to retrieve variant: "
                                + cartItem.variantId(),
                        exception
                );
            }

            if (variant == null) {

                throw new IllegalArgumentException(
                        "Variant not found: "
                                + cartItem.variantId()
                );
            }

            if (!variant.productId()
                    .equals(cartItem.productId())) {

                throw new IllegalArgumentException(
                        "Variant does not belong to product"
                );
            }

            // -----------------------------------------------------
            // Get Price
            // -----------------------------------------------------

            BigDecimal unitPrice =
                    variant.price();

            if (unitPrice == null) {

                throw new IllegalStateException(
                        "Product variant has no price: "
                                + cartItem.variantId()
                );
            }

            // -----------------------------------------------------
            // Calculate Item Total
            // -----------------------------------------------------

            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.quantity()
                            )
                    );

            // -----------------------------------------------------
            // Create Order Item
            // -----------------------------------------------------

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setProductId(
                    cartItem.productId()
            );

            orderItem.setVariantId(
                    cartItem.variantId()
            );

            orderItem.setQuantity(
                    cartItem.quantity()
            );

            orderItem.setUnitPrice(
                    unitPrice
            );

            orderItem.setTotalPrice(
                    itemTotal
            );

            order.addItem(orderItem);

            totalAmount =
                    totalAmount.add(itemTotal);
        }

        // =========================================================
        // 5. SET ORDER TOTAL
        // =========================================================

        order.setTotalAmount(
                totalAmount
        );

        // =========================================================
        // 6. RESERVE INVENTORY
        // =========================================================

        for (OrderItem orderItem :
                order.getItems()) {

            try {

                inventoryClient.reserve(
                        orderItem.getVariantId(),
                        new StockRequest(
                                orderItem.getQuantity()
                        )
                );

            } catch (Exception exception) {

                // -------------------------------------------------
                // Reservation failed
                // -------------------------------------------------

                throw new IllegalStateException(
                        "Unable to reserve inventory for variant: "
                                + orderItem.getVariantId(),
                        exception
                );
            }
        }

        // =========================================================
        // 7. INVENTORY RESERVED
        // =========================================================

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        // =========================================================
        // 8. SAVE ORDER
        // =========================================================

        Order savedOrder =
                orderRepository.save(order);

        // =========================================================
        // 9. RETURN RESPONSE
        // =========================================================

        return toResponse(savedOrder);
    }

    private CartResponse getCartResponse(Long userId) {
        CartResponse cart;

        try {

            cart = cartClient.getCart();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to retrieve cart",
                    exception
            );
        }

        // -----------------------------------------------------
        // 2. VALIDATE CART
        // -----------------------------------------------------

        if (cart == null) {

            throw new IllegalStateException(
                    "Cart not found"
            );
        }

        if (!cart.userId().equals(userId)) {

            throw new IllegalStateException(
                    "Cart does not belong to current user"
            );
        }
        return cart;
    }

    // =========================================================
    // GET ORDER
    // =========================================================

    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            Long userId,
            Long orderId
    ) {

        Order order =
                getOrderForUser(
                        userId,
                        orderId
                );

        return toResponse(order);
    }

    // =========================================================
    // GET MY ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getMyOrders(
            Long userId
    ) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // =========================================================
    // CANCEL ORDER
    // =========================================================

    public OrderResponse cancelOrder(
            Long userId,
            Long orderId
    ) {

        Order order =
                getOrderForUser(
                        userId,
                        orderId
                );

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Order cannot be cancelled in status: "
                            + order.getStatus()
            );
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        return toResponse(
                orderRepository.save(order)
        );
    }

    // =========================================================
    // INTERNAL
    // =========================================================

    private Order getOrderForUser(
            Long userId,
            Long orderId
    ) {

        return orderRepository
                .findById(orderId)
                .filter(order ->
                        order.getUserId()
                                .equals(userId)
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: "
                                        + orderId
                        )
                );
    }

    private String generateOrderNumber() {

        return "ORD-"
                + Instant.now().toEpochMilli()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private OrderResponse toResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getVariantId(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getTotalPrice()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderSummaryResponse toSummaryResponse(
            Order order
    ) {

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }


}