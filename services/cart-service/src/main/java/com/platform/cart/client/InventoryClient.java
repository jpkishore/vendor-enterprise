package com.platform.cart.client;

import com.platform.cart.dto.InventoryAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "inventory-service"
)
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/{variantId}/availability")
    InventoryAvailabilityResponse getAvailability(
            @PathVariable("variantId") Long variantId
    );
}