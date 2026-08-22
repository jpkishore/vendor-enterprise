package com.platform.cart.service;

import com.platform.cart.client.CatalogClient;
import com.platform.cart.client.InventoryClient;
import com.platform.cart.dto.*;
import com.platform.cart.entity.Cart;
import com.platform.cart.entity.CartItem;
import com.platform.cart.entity.CartStatus;
import com.platform.cart.repository.CartItemRepository;
import com.platform.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository, CatalogClient catalogClient, InventoryClient inventoryClient
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    // =========================================================
    // GET CURRENT USER CART
    // =========================================================

    @Transactional
    public CartResponse getCart(Long userId) {

        Cart cart = getOrCreateCart(userId);

        return toResponse(cart);
    }

    // =========================================================
    // ADD ITEM
    // =========================================================
    public CartResponse addItem(
            Long userId,
            CartAddItemRequest request
    ) {

        // =========================================================
        // 1. VALIDATE PRODUCT FROM CATALOG SERVICE
        // =========================================================

        CatalogProductResponse product;

        try {

            product = catalogClient.getProduct(
                    request.productId()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to validate product with Catalog Service",
                    exception
            );
        }

        if (product == null) {

            throw new IllegalArgumentException(
                    "Product not found: "
                            + request.productId()
            );
        }

        // =========================================================
        // 2. VALIDATE VARIANT FROM CATALOG SERVICE
        // =========================================================

        CatalogVariantResponse variant;

        try {

            variant = catalogClient.getVariant(
                    request.productId(),
                    request.variantId()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to validate variant with Catalog Service",
                    exception
            );
        }

        if (variant == null) {

            throw new IllegalArgumentException(
                    "Variant not found: "
                            + request.variantId()
            );
        }

        if (!variant.productId()
                .equals(request.productId())) {

            throw new IllegalArgumentException(
                    "Variant does not belong to product"
            );
        }

        // =========================================================
        // 3. GET / CREATE CART
        // =========================================================

        Cart cart = getOrCreateCart(userId);

        if (cart.getStatus() != CartStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Cart is not active"
            );
        }

        // =========================================================
        // 4. FIND EXISTING ITEM
        // =========================================================

        CartItem item =
                cartItemRepository
                        .findByCartIdAndVariantId(
                                cart.getId(),
                                request.variantId()
                        )
                        .orElse(null);

        // =========================================================
        // 5. CALCULATE FINAL QUANTITY
        // =========================================================

        int existingQuantity =
                item == null
                        ? 0
                        : item.getQuantity();

        int finalQuantity =
                existingQuantity
                        + request.quantity();

        // =========================================================
        // 6. CHECK INVENTORY
        // =========================================================

        InventoryAvailabilityResponse inventory;

        try {

            inventory =
                    inventoryClient.getAvailability(
                            request.variantId()
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to check inventory availability",
                    exception
            );
        }

        if (inventory == null) {

            throw new IllegalArgumentException(
                    "Inventory not found for variant: "
                            + request.variantId()
            );
        }

        if (inventory.availableQuantity()
                < finalQuantity) {

            throw new IllegalStateException(
                    "Insufficient stock. Available: "
                            + inventory.availableQuantity()
                            + ", requested: "
                            + finalQuantity
            );
        }

        // =========================================================
        // 7. ADD NEW ITEM
        // =========================================================

        if (item == null) {

            item = new CartItem();

            item.setProductId(
                    request.productId()
            );

            item.setVariantId(
                    request.variantId()
            );

            item.setQuantity(
                    request.quantity()
            );

            cart.addItem(item);
        }

        // =========================================================
        // 8. UPDATE EXISTING ITEM
        // =========================================================

        else {

            item.setQuantity(
                    finalQuantity
            );
        }

        // =========================================================
        // 9. SAVE CART
        // =========================================================

        cartRepository.save(cart);

        return toResponse(cart);
    }
    // =========================================================
    // UPDATE ITEM
    // =========================================================

    public CartResponse updateItem(
            Long userId,
            Long variantId,
            CartUpdateItemRequest request
    ) {

        // =========================================================
        // 1. GET ACTIVE CART
        // =========================================================

        Cart cart = getCartForUser(userId);

        // =========================================================
        // 2. FIND CART ITEM
        // =========================================================

        CartItem item =
                cartItemRepository
                        .findByCartIdAndVariantId(
                                cart.getId(),
                                variantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Cart item not found for variant: "
                                                + variantId
                                )
                        );

        // =========================================================
        // 3. VALIDATE VARIANT WITH CATALOG SERVICE
        // =========================================================

        CatalogVariantResponse variant;

        try {

            variant =
                    catalogClient.getVariant(
                            item.getProductId(),
                            variantId
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to validate variant with Catalog Service",
                    exception
            );
        }

        if (variant == null) {

            throw new IllegalArgumentException(
                    "Variant not found: "
                            + variantId
            );
        }

        if (!variant.productId()
                .equals(item.getProductId())) {

            throw new IllegalArgumentException(
                    "Variant does not belong to product"
            );
        }

        // =========================================================
        // 4. CHECK INVENTORY
        // =========================================================

        InventoryAvailabilityResponse inventory;

        try {

            inventory =
                    inventoryClient.getAvailability(
                            variantId
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to check inventory availability",
                    exception
            );
        }

        if (inventory == null) {

            throw new IllegalArgumentException(
                    "Inventory not found for variant: "
                            + variantId
            );
        }

        // =========================================================
        // 5. CHECK AVAILABLE STOCK
        // =========================================================

        if (inventory.availableQuantity()
                < request.quantity()) {

            throw new IllegalStateException(
                    "Insufficient stock. Available: "
                            + inventory.availableQuantity()
                            + ", requested: "
                            + request.quantity()
            );
        }

        // =========================================================
        // 6. UPDATE QUANTITY
        // =========================================================

        item.setQuantity(
                request.quantity()
        );

        // =========================================================
        // 7. SAVE
        // =========================================================

        cartItemRepository.save(item);

        return toResponse(cart);
    }

    // =========================================================
    // REMOVE ITEM
    // =========================================================

    public CartResponse removeItem(
            Long userId,
            Long variantId
    ) {

        Cart cart = getCartForUser(userId);

        CartItem item =
                cartItemRepository
                        .findByCartIdAndVariantId(
                                cart.getId(),
                                variantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Cart item not found for variant: "
                                                + variantId
                                )
                        );

        cart.removeItem(item);

        cartItemRepository.delete(item);

        return toResponse(cart);
    }

    // =========================================================
    // CLEAR CART
    // =========================================================

    public void clearCart(Long userId) {

        Cart cart = getCartForUser(userId);

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    // =========================================================
    // INTERNAL METHODS
    // =========================================================

    private Cart getOrCreateCart(Long userId) {

        return cartRepository
                .findByUserIdAndStatus(
                        userId,
                        CartStatus.ACTIVE
                )
                .orElseGet(() -> {

                    Cart cart = new Cart();

                    cart.setUserId(userId);
                    cart.setStatus(
                            CartStatus.ACTIVE
                    );

                    return cartRepository.save(cart);
                });
    }

    private Cart getCartForUser(Long userId) {

        return cartRepository
                .findByUserIdAndStatus(
                        userId,
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active cart not found"
                        )
                );
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private CartResponse toResponse(Cart cart) {

        List<CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus(),
                items,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private CartItemResponse toItemResponse(
            CartItem item
    ) {

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getQuantity()
        );
    }
}