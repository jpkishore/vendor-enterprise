package com.platform.cart.controller;

import com.platform.cart.dto.CartAddItemRequest;
import com.platform.cart.dto.CartResponse;
import com.platform.cart.dto.CartUpdateItemRequest;
import com.platform.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // =========================================================
    // GET CART
    // =========================================================

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody CartAddItemRequest request
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.addItem(
                        userId,
                        request
                )
        );
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    @PutMapping("/items/{variantId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable Long variantId,
            @Valid @RequestBody CartUpdateItemRequest request
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.updateItem(
                        userId,
                        variantId,
                        request
                )
        );
    }

    // =========================================================
    // REMOVE ITEM
    // =========================================================

    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(
            Authentication authentication,
            @PathVariable Long variantId
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                cartService.removeItem(
                        userId,
                        variantId
                )
        );
    }

    // =========================================================
    // CLEAR CART
    // =========================================================

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // JWT USER ID
    // =========================================================

    private Long getUserId(
            Authentication authentication
    ) {

        return Long.valueOf(
                authentication.getName()
        );
    }
}