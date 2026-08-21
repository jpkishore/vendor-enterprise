package com.platform.catalog.controller;

import com.platform.catalog.dto.product.ProductVariantCreateRequest;
import com.platform.catalog.dto.product.ProductVariantResponse;
import com.platform.catalog.dto.product.ProductVariantUpdateRequest;
import com.platform.catalog.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductVariantResponse>> findAll(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productVariantService.findAll(productId)
        );
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> findById(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {

        return ResponseEntity.ok(
                productVariantService.findById(
                        productId,
                        variantId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductVariantResponse> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productVariantService.create(
                                productId,
                                request
                        )
                );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> update(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request
    ) {

        return ResponseEntity.ok(
                productVariantService.update(
                        productId,
                        variantId,
                        request
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long variantId
    ) {

        productVariantService.delete(
                productId,
                variantId
        );

        return ResponseEntity.noContent().build();
    }
}