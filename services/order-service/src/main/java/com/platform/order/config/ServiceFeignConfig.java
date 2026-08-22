package com.platform.order.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ServiceFeignConfig {

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

            log.info(
                    "Feign internal service token added for: {} {}",
                    requestTemplate.method(),
                    requestTemplate.url()
            );
        };


    }
}