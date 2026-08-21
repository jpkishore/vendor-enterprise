package com.platform.inventory.repository;

import com.platform.inventory.entity.Inventory;
import com.platform.inventory.entity.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantId(
            Long variantId
    );

    boolean existsByVariantId(
            Long variantId
    );

    List<Inventory> findByProductId(
            Long productId
    );

    List<Inventory> findByStatus(
            InventoryStatus status
    );
}