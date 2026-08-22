package com.platform.order.client;

import com.platform.order.config.FeignClientConfig;
import com.platform.order.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "cart-service",
        configuration = FeignClientConfig.class
)
public interface CartClient {

    @GetMapping("/api/v1/cart")
    CartResponse getCart();
}