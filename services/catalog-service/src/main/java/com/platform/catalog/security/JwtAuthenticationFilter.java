package com.platform.catalog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
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

        // No Authorization header
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {

            // Validate JWT
            if (!jwtService.isTokenValid(token)) {
                System.out.println("JWT TOKEN INVALID");

                filterChain.doFilter(request, response);
                return;
            }

            String username =
                    jwtService.extractUsername(token);

            /*
             * JWT:
             *
             * "roles": ["CUSTOMER"]
             *
             * Spring Security:
             *
             * ROLE_CUSTOMER
             */
            List<String> roles =
                    jwtService.extractRoles(token);
           var authorities =
                    roles
                            .stream()
                            .map(role ->
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                            .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println(
                    "JWT USERNAME = " + username
            );

            System.out.println(
                    "JWT AUTHORITIES = " + authorities
            );

        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();

            System.out.println(
                    "JWT authentication failed: "
                            + exception.getMessage()
            );
        }

        filterChain.doFilter(request, response);
    }
}