package com.platform.payment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // =====================================================
        // NO JWT
        // =====================================================

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {

            // =================================================
            // VALIDATE JWT
            // =================================================

            if (!jwtService.isTokenValid(token)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            // =================================================
            // EXTRACT USER INFORMATION
            // =================================================

            String username =
                    jwtService.extractUsername(token);

            Long userId =
                    jwtService.extractUserId(token);

            List<String> roles =
                    jwtService.extractRoles(token);

            // =================================================
            // AUTHORITIES
            // =================================================

            var authorities =
                    roles.stream()
                            .map(role ->
                                    new SimpleGrantedAuthority(
                                            role.startsWith("ROLE_")
                                                    ? role
                                                    : "ROLE_" + role
                                    )
                            )
                            .toList();

            // =================================================
            // AUTHENTICATION
            // =================================================

            /*
             * Store userId as principal.
             *
             * This allows:
             *
             * authentication.getPrincipal()
             *
             * to return Long userId.
             */

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            /*
             * Keep username as request details is NOT recommended.
             * WebAuthenticationDetails is only request metadata.
             */

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            System.out.println(
                    "PAYMENT JWT USERNAME = "
                            + username
            );

            System.out.println(
                    "PAYMENT JWT USER ID = "
                            + userId
            );

            System.out.println(
                    "PAYMENT JWT AUTHORITIES = "
                            + authorities
            );

        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();

            System.out.println(
                    "Payment JWT authentication failed: "
                            + exception.getMessage()
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}