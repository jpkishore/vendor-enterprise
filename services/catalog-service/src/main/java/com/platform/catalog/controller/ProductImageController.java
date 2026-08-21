package com.platform.catalog.controller;

import com.platform.catalog.dto.product.ProductImageCreateRequest;
import com.platform.catalog.dto.product.ProductImageResponse;
import com.platform.catalog.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> findAll(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productImageService.findAll(productId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductImageResponse> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productImageService.create(
                                productId,
                                request
                        )
                );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {

        productImageService.delete(
                productId,
                imageId
        );

        return ResponseEntity.noContent().build();
    }
}