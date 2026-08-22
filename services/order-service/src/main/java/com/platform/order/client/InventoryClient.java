package com.platform.order.client;

import com.platform.order.config.ServiceFeignConfig;
import com.platform.order.dto.InventoryResponse;
import com.platform.order.dto.StockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        configuration = ServiceFeignConfig.class
)
public interface InventoryClient {

    @PostMapping(
            "/api/v1/inventory/{variantId}/reserve"
    )
    InventoryResponse reserve(
            @PathVariable("variantId") Long variantId,
            @RequestBody StockRequest request
    );

    @PostMapping(
            "/api/v1/inventory/{variantId}/release"
    )
    InventoryResponse release(
            @PathVariable("variantId") Long variantId,
            @RequestBody StockRequest request
    );
}