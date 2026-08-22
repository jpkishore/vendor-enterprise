package com.platform.payment.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor serviceRequestInterceptor(
            @Value("${security.service.token}")
            String serviceToken
    ) {

        return requestTemplate -> {

            requestTemplate.header(
                    "X-Service-Token",
                    serviceToken
            );
        };
    }
}