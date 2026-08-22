package com.platform.cart.repository;

import com.platform.cart.entity.Cart;
import com.platform.cart.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository
        extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndStatus(
            Long userId,
            CartStatus status
    );

    boolean existsByUserId(Long userId);
}