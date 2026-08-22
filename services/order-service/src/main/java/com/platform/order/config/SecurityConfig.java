package com.platform.order.config;

import com.platform.order.security.JwtAuthenticationFilter;
import com.platform.order.security.ServiceTokenFilter;
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
                        // ACTUATOR
                        // =================================

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        )
                        .permitAll()

                        // =================================
                        // CREATE ORDER
                        // CUSTOMER / ADMIN
                        // =================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/orders"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =================================
                        // GET ORDERS
                        //
                        // CUSTOMER can access
                        // INTERNAL_SERVICE can access
                        // =================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/orders",
                                "/api/v1/orders/**"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "INTERNAL_SERVICE"
                        )

                        // =================================
                        // CANCEL ORDER
                        // =================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/orders/*/cancel"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =================================
                        // EVERYTHING ELSE
                        // =================================

                        .anyRequest()
                        .authenticated()
                )

                // =========================================
                // INTERNAL SERVICE TOKEN
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