package com.platform.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ServiceTokenFilter extends OncePerRequestFilter {

    private static final String SERVICE_TOKEN_HEADER =
            "X-Service-Token";

    private final String serviceToken;

    public ServiceTokenFilter(
            @Value("${security.service.token}")
            String serviceToken
    ) {
        this.serviceToken = serviceToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token =
                request.getHeader(
                        SERVICE_TOKEN_HEADER
                );

        System.out.println(
                "SERVICE TOKEN HEADER PRESENT = "
                        + (token != null)
        );

        if (token != null
                && token.equals(serviceToken)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "order-service",
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_INTERNAL_SERVICE"
                                    )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            System.out.println(
                    "INTERNAL SERVICE AUTHENTICATION SUCCESS"
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}