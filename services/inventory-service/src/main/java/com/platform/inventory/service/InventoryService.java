package com.platform.inventory.service;

import com.platform.inventory.dto.*;
import com.platform.inventory.entity.Inventory;
import com.platform.inventory.entity.InventoryStatus;
import com.platform.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // =========================================================
    // CREATE INVENTORY
    // =========================================================

    public InventoryResponse create(
            InventoryCreateRequest request
    ) {

        if (inventoryRepository.existsByVariantId(
                request.variantId()
        )) {
            throw new IllegalStateException(
                    "Inventory already exists for variant: "
                            + request.variantId()
            );
        }

        Inventory inventory = new Inventory();

        inventory.setProductId(request.productId());
        inventory.setVariantId(request.variantId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(
                request.reorderLevel() == null
                        ? 10
                        : request.reorderLevel()
        );
        inventory.setStatus(
                request.quantity() > 0
                        ? InventoryStatus.ACTIVE
                        : InventoryStatus.OUT_OF_STOCK
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Transactional(readOnly = true)
    public List<InventoryResponse> findAll() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY VARIANT
    // =========================================================

    @Transactional(readOnly = true)
    public InventoryResponse findByVariantId(
            Long variantId
    ) {

        return toResponse(
                getInventory(variantId)
        );
    }

    // =========================================================
    // UPDATE INVENTORY
    // =========================================================

    public InventoryResponse update(
            Long variantId,
            InventoryUpdateRequest request
    ) {

        Inventory inventory =
                getInventory(variantId);

        if (request.quantity()
                < inventory.getReservedQuantity()) {

            throw new IllegalStateException(
                    "Quantity cannot be less than reserved quantity"
            );
        }

        inventory.setQuantity(
                request.quantity()
        );

        inventory.setReorderLevel(
                request.reorderLevel()
        );

        updateStatus(inventory);

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    // =========================================================
    // AVAILABILITY
    // =========================================================

    @Transactional(readOnly = true)
    public InventoryResponse availability(
            Long variantId
    ) {

        return toResponse(
                getInventory(variantId)
        );
    }

    // =========================================================
    // RESERVE STOCK
    // =========================================================

    public InventoryResponse reserve(
            Long variantId,
            StockRequest request
    ) {

        Inventory inventory =
                getInventory(variantId);

        int available =
                inventory.getQuantity()
                        - inventory.getReservedQuantity();

        if (available < request.quantity()) {

            throw new IllegalStateException(
                    "Insufficient stock. Available: "
                            + available
                            + ", requested: "
                            + request.quantity()
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        + request.quantity()
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    // =========================================================
    // RELEASE STOCK
    // =========================================================

    public InventoryResponse release(
            Long variantId,
            StockRequest request
    ) {

        Inventory inventory =
                getInventory(variantId);

        int reserved =
                inventory.getReservedQuantity();

        if (reserved < request.quantity()) {

            throw new IllegalStateException(
                    "Cannot release more than reserved quantity. "
                            + "Reserved: "
                            + reserved
            );
        }

        inventory.setReservedQuantity(
                reserved - request.quantity()
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    // =========================================================
    // ADJUST STOCK
    // =========================================================

    public InventoryResponse adjust(
            Long variantId,
            StockAdjustmentRequest request
    ) {

        Inventory inventory =
                getInventory(variantId);

        int newQuantity =
                inventory.getQuantity()
                        + request.quantityChange();

        if (newQuantity < 0) {

            throw new IllegalStateException(
                    "Inventory quantity cannot be negative"
            );
        }

        if (newQuantity
                < inventory.getReservedQuantity()) {

            throw new IllegalStateException(
                    "Inventory quantity cannot be less than "
                            + "reserved quantity"
            );
        }

        inventory.setQuantity(newQuantity);

        updateStatus(inventory);

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    // =========================================================
    // INTERNAL METHODS
    // =========================================================

    private Inventory getInventory(
            Long variantId
    ) {

        return inventoryRepository
                .findByVariantId(variantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory not found for variant: "
                                        + variantId
                        )
                );
    }

    private void updateStatus(
            Inventory inventory
    ) {

        if (inventory.getQuantity() == 0) {

            inventory.setStatus(
                    InventoryStatus.OUT_OF_STOCK
            );

        } else {

            inventory.setStatus(
                    InventoryStatus.ACTIVE
            );
        }
    }

    private InventoryResponse toResponse(
            Inventory inventory
    ) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getVariantId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getReorderLevel(),
                inventory.getStatus(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}