package com.platform.cart.config;

import com.platform.cart.security.JwtAuthenticationFilter;
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

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

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