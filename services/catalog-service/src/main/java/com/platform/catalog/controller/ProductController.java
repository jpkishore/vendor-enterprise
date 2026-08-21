package com.platform.catalog.controller;

import com.platform.catalog.dto.product.ProductCreateRequest;
import com.platform.catalog.dto.product.ProductPageResponse;
import com.platform.catalog.dto.product.ProductResponse;
import com.platform.catalog.dto.product.ProductUpdateRequest;
import com.platform.catalog.entity.enums.ProductStatus;
import com.platform.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ProductPageResponse> findAll(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "name")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            ProductStatus status

    ) {

        return ResponseEntity.ok(
                productService.findAll(
                        page,
                        size,
                        sortBy,
                        direction,
                        categoryId,
                        status
                )
        );
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                productService.findById(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {

        return ResponseEntity.ok(
                productService.update(id, request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        productService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ProductPageResponse> search(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "name")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            ProductStatus status

    ) {

        return ResponseEntity.ok(
                productService.search(
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction,
                        categoryId,
                        status
                )
        );
    }
}