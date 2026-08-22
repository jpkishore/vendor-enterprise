package com.platform.order.client;

import com.platform.order.config.FeignClientConfig;
import com.platform.order.dto.CatalogProductResponse;
import com.platform.order.dto.CatalogVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "catalog-service",
        configuration = FeignClientConfig.class
)
public interface CatalogClient {

    @GetMapping("/api/v1/products/{id}")
    CatalogProductResponse getProduct(
            @PathVariable("id") Long productId
    );

    @GetMapping(
            "/api/v1/products/{productId}/variants/{variantId}"
    )
    CatalogVariantResponse getVariant(
            @PathVariable("productId") Long productId,
            @PathVariable("variantId") Long variantId
    );
}