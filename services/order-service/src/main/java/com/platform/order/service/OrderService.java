package com.platform.order.service;

import com.platform.order.client.CartClient;
import com.platform.order.client.CatalogClient;
import com.platform.order.client.InventoryClient;
import com.platform.order.dto.*;
import com.platform.order.entity.*;
import com.platform.order.kafka.OrderEventProducer;
import com.platform.order.repository.IdempotencyKeyRepository;
import com.platform.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final OrderEventProducer orderEventProducer;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    public OrderService(
            OrderRepository orderRepository,
            CartClient cartClient,
            CatalogClient catalogClient,
            InventoryClient inventoryClient, OrderEventProducer orderEventProducer, IdempotencyKeyRepository idempotencyKeyRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.orderEventProducer = orderEventProducer;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    public OrderResponse createOrder(
            Long userId,
            String idempotencyKey
    ){
        // =====================================================
        // 1. GET CUSTOMER CART
        // =====================================================
        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }
        IdempotencyKey existing =
                idempotencyKeyRepository
                        .findByUserIdAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
                        .orElse(null);

        if (existing != null) {

            if (existing.getOrderId() != null) {

                Order existingOrder =
                        orderRepository
                                .findById(
                                        existing.getOrderId()
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Order associated with idempotency key not found"
                                        )
                                );

                return toResponse(existingOrder);
            }

            throw new IllegalStateException(
                    "Order creation is already in progress"
            );
        }
        IdempotencyKey key =
                new IdempotencyKey();

        key.setUserId(userId);

        key.setIdempotencyKey(
                idempotencyKey
        );

        key.setCreatedAt(
                Instant.now()
        );



        CartResponse cart;

        try {

            cart = cartClient.getCart();

        } catch (Exception exception) {

            log.error(
                    "Unable to retrieve cart for user={}",
                    userId,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to retrieve cart",
                    exception
            );
        }

        // =====================================================
        // 2. VALIDATE CART
        // =====================================================

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

        // =====================================================
        // 3. CREATE ORDER IN MEMORY
        // =====================================================

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

        // =====================================================
        // 4. PROCESS CART ITEMS
        // =====================================================

        for (CartItemResponse cartItem :
                cart.items()) {

            // =================================================
            // 4.1 VALIDATE PRODUCT
            // =================================================

            CatalogProductResponse product;

            try {

                product =
                        catalogClient.getProduct(
                                cartItem.productId()
                        );

            } catch (Exception exception) {

                log.error(
                        "Unable to validate product={}",
                        cartItem.productId(),
                        exception
                );

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

            // =================================================
            // 4.2 VALIDATE VARIANT
            // =================================================

            CatalogVariantResponse variant;

            try {

                variant =
                        catalogClient.getVariant(
                                cartItem.productId(),
                                cartItem.variantId()
                        );

            } catch (Exception exception) {

                log.error(
                        "Unable to retrieve variant={}",
                        cartItem.variantId(),
                        exception
                );

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

            // =================================================
            // 4.3 VALIDATE PRODUCT / VARIANT RELATIONSHIP
            // =================================================

            if (!variant.productId()
                    .equals(cartItem.productId())) {

                throw new IllegalArgumentException(
                        "Variant does not belong to product"
                );
            }

            // =================================================
            // 4.4 GET CURRENT PRICE
            // =================================================

            BigDecimal unitPrice =
                    variant.price();

            if (unitPrice == null) {

                throw new IllegalStateException(
                        "Product variant has no price: "
                                + cartItem.variantId()
                );
            }

            // =================================================
            // 4.5 VALIDATE QUANTITY
            // =================================================

            if (cartItem.quantity() == null
                    || cartItem.quantity() <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero"
                );
            }

            // =================================================
            // 4.6 CALCULATE ITEM TOTAL
            // =================================================

            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.quantity()
                            )
                    );

            // =================================================
            // 4.7 CREATE ORDER ITEM
            // =================================================

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

        // =====================================================
        // 5. SET ORDER TOTAL
        // =====================================================

        order.setTotalAmount(
                totalAmount
        );

        log.info(
                "Order prepared. user={}, orderNumber={}, total={}",
                userId,
                order.getOrderNumber(),
                totalAmount
        );

        // =====================================================
        // 6. RESERVE INVENTORY
        // =====================================================

        List<OrderItem> reservedItems =
                new ArrayList<>();

        try {

            for (OrderItem orderItem :
                    order.getItems()) {

                log.info(
                        "Reserving inventory. variant={}, quantity={}",
                        orderItem.getVariantId(),
                        orderItem.getQuantity()
                );

                inventoryClient.reserve(
                        orderItem.getVariantId(),
                        new StockRequest(
                                orderItem.getQuantity()
                        )
                );

                reservedItems.add(orderItem);

                log.info(
                        "Inventory reserved successfully. variant={}, quantity={}",
                        orderItem.getVariantId(),
                        orderItem.getQuantity()
                );
            }

        } catch (Exception exception) {

            log.error(
                    "Inventory reservation failed. Starting rollback. orderNumber={}",
                    order.getOrderNumber(),
                    exception
            );

            releaseReservedInventory(
                    reservedItems
            );

            throw new IllegalStateException(
                    "Unable to reserve inventory",
                    exception
            );
        }

        // =====================================================
        // 7. INVENTORY RESERVED
        // =====================================================

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        log.info(
                "Inventory reservation successful. Confirming order={}",
                order.getOrderNumber()
        );

        // =====================================================
        // 8. SAVE ORDER
        // =====================================================

        Order savedOrder =
                orderRepository.save(order);
        key.setOrderId(
                savedOrder.getId()
        );

        key.setStatus(
                IdempotencyStatus.COMPLETED
        );

        key.setUpdatedAt(
                Instant.now()
        );

        idempotencyKeyRepository.save(key);
        log.info(
                "Order created successfully. orderId={}, orderNumber={}, user={}",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                userId
        );

        // =====================================================
        // 9. RETURN RESPONSE
        // =====================================================

        OrderEvent event =
                buildOrderEvent(savedOrder);

        orderEventProducer.publish(event);

        return toResponse(savedOrder);    }
    private OrderEvent buildOrderEvent(
            Order order
    ) {

        List<OrderEventItem> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderEventItem(
                                        item.getProductId(),
                                        item.getVariantId(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getTotalPrice()
                                )
                        )
                        .toList();

        return new OrderEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                items,
                Instant.now()
        );
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

        // =====================================================
        // VALIDATE ORDER STATUS
        // =====================================================
        if (order.getStatus() == OrderStatus.CANCELLED) {

            return toResponse(order);
        }
        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Order cannot be cancelled in status: "
                            + order.getStatus()
            );
        }

        // =====================================================
        // RELEASE INVENTORY
        // =====================================================

        releaseReservedInventory(
                order.getItems()
        );

        // =====================================================
        // UPDATE ORDER STATUS
        // =====================================================

        order.setStatus(
                OrderStatus.CANCELLED
        );

        Order savedOrder =
                orderRepository.save(order);

        log.info(
                "Order cancelled successfully. orderId={}, user={}",
                orderId,
                userId
        );

        return toResponse(savedOrder);
    }

    // =========================================================
    // RELEASE RESERVED INVENTORY
    // =========================================================

    private void releaseReservedInventory(
            List<OrderItem> reservedItems
    ) {

        if (reservedItems == null
                || reservedItems.isEmpty()) {

            return;
        }

        for (OrderItem item :
                reservedItems) {

            try {

                inventoryClient.release(
                        item.getVariantId(),
                        new StockRequest(
                                item.getQuantity()
                        )
                );

                log.info(
                        "Inventory released. variant={}, quantity={}",
                        item.getVariantId(),
                        item.getQuantity()
                );

            } catch (Exception exception) {

                log.error(
                        "Failed to release inventory. variant={}, quantity={}",
                        item.getVariantId(),
                        item.getQuantity(),
                        exception
                );
            }
        }
    }

    // =========================================================
    // GET ORDER FOR USER
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

    // =========================================================
    // GENERATE ORDER NUMBER
    // =========================================================

    private String generateOrderNumber() {

        return "ORD-"
                + Instant.now().toEpochMilli()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    // =========================================================
    // CONVERT TO ORDER RESPONSE
    // =========================================================

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

    // =========================================================
    // CONVERT TO SUMMARY RESPONSE
    // =========================================================

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

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: " + orderId
                                )
                        );

        return toResponse(order);
    }
}