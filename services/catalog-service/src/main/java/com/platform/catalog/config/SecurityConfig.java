package com.platform.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // CUSTOMER + ADMIN + SUPER_ADMIN
                        // Can read categories
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categories",
                                "/api/v1/categories/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // ADMIN + SUPER_ADMIN
                        // Can create category
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/categories"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // ADMIN + SUPER_ADMIN
                        // Can update category
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/categories/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // ADMIN + SUPER_ADMIN
                        // Can delete category
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/categories/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}