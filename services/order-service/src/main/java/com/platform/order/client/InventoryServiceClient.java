package com.platform.order.client;

import com.platform.order.dto.InventoryResponse;
import com.platform.order.dto.StockRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class InventoryServiceClient {

    private final InventoryClient inventoryClient;

    public InventoryServiceClient(
            InventoryClient inventoryClient
    ) {
        this.inventoryClient =
                inventoryClient;
    }

    @CircuitBreaker(
            name = "inventoryService",
            fallbackMethod = "reserveFallback"
    )
    public void reserve(
            Long inventoryId,
            StockRequest request
    ) {

        inventoryClient.reserve(
                inventoryId,
                request
        );
    }

    @CircuitBreaker(
            name = "inventoryService",
            fallbackMethod = "reserveFallback"
    )
    public void  release(
            Long variantId,
            StockRequest request
    ){
        inventoryClient.release(
                variantId,
                request
        );
    }
    private void reserveFallback(
            Long inventoryId,
            StockRequest request,
            Throwable throwable
    ) {

        throw new IllegalStateException(
                "Inventory service is currently unavailable",
                throwable
        );
    }
}