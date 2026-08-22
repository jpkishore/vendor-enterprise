package com.platform.cart.client;

import com.platform.cart.dto.CatalogProductResponse;
import com.platform.cart.dto.CatalogVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "catalog-service"
)
public interface CatalogClient {

    @GetMapping("/api/v1/products/{id}")
    CatalogProductResponse getProduct(
            @PathVariable("id") Long productId
    );

    @GetMapping("/api/v1/products/{productId}/variants/{variantId}")
    CatalogVariantResponse getVariant(
            @PathVariable("productId") Long productId,
            @PathVariable("variantId") Long variantId
    );
}