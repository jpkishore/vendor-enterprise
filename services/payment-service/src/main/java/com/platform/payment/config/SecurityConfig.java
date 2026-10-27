package com.platform.payment.config;

import com.platform.payment.security.JwtAuthenticationFilter;
import com.platform.payment.security.ServiceTokenFilter;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.annotation.Order;
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

    // Actuator security
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    // Application security
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http

                // =========================================
                // CSRF
                // =========================================

                .csrf(csrf ->
                        csrf.disable()
                )

                // =========================================
                // SESSION
                // =========================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =========================================
                // AUTHORIZATION
                // =========================================

                .authorizeHttpRequests(auth -> auth

                        // =================================
                        // CUSTOMER PAYMENT
                        // =================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/payments"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =================================
                        // GET PAYMENT
                        // =================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/payments",
                                "/api/v1/payments/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "INTERNAL_SERVICE"
                        )

                        // =================================
                        // INTERNAL SERVICES
                        // =================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/payments/*/process"
                        ).hasRole(
                                "INTERNAL_SERVICE"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/payments/*/refund"
                        ).hasRole(
                                "INTERNAL_SERVICE"
                        )

                        // =================================
                        // EVERYTHING ELSE
                        // =================================

                        .anyRequest()
                        .authenticated()
                )

                // =========================================
                // SERVICE TOKEN
                // =========================================

                .addFilterBefore(
                        serviceTokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // =========================================
                // JWT
                // =========================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}