package com.platform.payment.client;

import com.platform.payment.config.FeignClientConfig;
import com.platform.payment.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
        configuration = FeignClientConfig.class
)
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderResponse getOrder(
            @PathVariable("orderId")
            Long orderId
    );
}