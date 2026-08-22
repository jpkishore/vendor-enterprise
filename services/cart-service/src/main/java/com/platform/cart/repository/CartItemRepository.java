package com.platform.cart.repository;

import com.platform.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndVariantId(
            Long cartId,
            Long variantId
    );

    boolean existsByCartIdAndVariantId(
            Long cartId,
            Long variantId
    );

    void deleteByCartIdAndVariantId(
            Long cartId,
            Long variantId
    );
}