package com.platform.order.repository;

import com.platform.order.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey>
    findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );
}