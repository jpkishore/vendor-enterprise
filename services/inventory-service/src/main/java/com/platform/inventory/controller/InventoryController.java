package com.platform.inventory.controller;

import com.platform.inventory.dto.InventoryCreateRequest;
import com.platform.inventory.dto.InventoryResponse;
import com.platform.inventory.dto.InventoryUpdateRequest;
import com.platform.inventory.dto.StockAdjustmentRequest;
import com.platform.inventory.dto.StockRequest;
import com.platform.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // =========================================================
    // CREATE INVENTORY
    // =========================================================

    @PostMapping
    public ResponseEntity<InventoryResponse> create(
            @Valid @RequestBody InventoryCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.create(request));
    }

    // =========================================================
    // GET ALL INVENTORY
    // =========================================================

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> findAll() {

        return ResponseEntity.ok(
                inventoryService.findAll()
        );
    }

    // =========================================================
    // GET INVENTORY BY VARIANT
    // =========================================================

    @GetMapping("/{variantId}")
    public ResponseEntity<InventoryResponse> findByVariantId(
            @PathVariable Long variantId
    ) {

        return ResponseEntity.ok(
                inventoryService.findByVariantId(variantId)
        );
    }

    // =========================================================
    // UPDATE INVENTORY
    // =========================================================

    @PutMapping("/{variantId}")
    public ResponseEntity<InventoryResponse> update(
            @PathVariable Long variantId,
            @Valid @RequestBody InventoryUpdateRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.update(
                        variantId,
                        request
                )
        );
    }

    // =========================================================
    // CHECK AVAILABILITY
    // =========================================================

    @GetMapping("/{variantId}/availability")
    public ResponseEntity<InventoryResponse> availability(
            @PathVariable Long variantId
    ) {

        return ResponseEntity.ok(
                inventoryService.availability(
                        variantId
                )
        );
    }

    // =========================================================
    // RESERVE STOCK
    // =========================================================

    @PostMapping("/{variantId}/reserve")
    public ResponseEntity<InventoryResponse> reserve(
            @PathVariable Long variantId,
            @Valid @RequestBody StockRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.reserve(
                        variantId,
                        request
                )
        );
    }

    // =========================================================
    // RELEASE STOCK
    // =========================================================

    @PostMapping("/{variantId}/release")
    public ResponseEntity<InventoryResponse> release(
            @PathVariable Long variantId,
            @Valid @RequestBody StockRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.release(
                        variantId,
                        request
                )
        );
    }

    // =========================================================
    // ADJUST STOCK
    // =========================================================

    @PostMapping("/{variantId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(
            @PathVariable Long variantId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.adjust(
                        variantId,
                        request
                )
        );
    }
}