package com.platform.inventory.config;

import com.platform.inventory.security.JwtAuthenticationFilter;
import com.platform.inventory.security.ServiceTokenFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ServiceTokenFilter serviceTokenFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ServiceTokenFilter serviceTokenFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.serviceTokenFilter =
                serviceTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // ACTUATOR
                        // =====================================

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // =====================================
                        // CUSTOMER / ADMIN READ
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/inventory",
                                "/api/v1/inventory/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================
                        // ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/inventory/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory/*/adjust"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================
                        // INTERNAL ORDER SERVICE
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory/*/reserve"
                        ).hasRole(
                                "INTERNAL_SERVICE"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory/*/release"
                        ).hasRole(
                                "INTERNAL_SERVICE"
                        )

                        // =====================================
                        // EVERYTHING ELSE
                        // =====================================

                        .anyRequest().authenticated()
                )

                // =============================================
                // JWT FILTER
                // =============================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // =============================================
                // SERVICE TOKEN FILTER
                // =============================================

                .addFilterAfter(
                        serviceTokenFilter,
                        JwtAuthenticationFilter.class
                )

                .build();
    }
}