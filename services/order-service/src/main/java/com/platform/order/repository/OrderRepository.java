package com.platform.order.repository;

import com.platform.order.entity.Order;
import com.platform.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(
            String orderNumber
    );

    List<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            OrderStatus status
    );

    boolean existsByOrderNumber(
            String orderNumber
    );
}