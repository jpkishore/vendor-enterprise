package com.platform.cart.config;

import com.platform.cart.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
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
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // CUSTOMER + ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cart"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/cart/items"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/cart/items/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/cart/items/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/cart"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}