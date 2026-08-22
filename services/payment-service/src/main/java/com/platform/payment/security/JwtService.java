package com.platform.payment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
            @Value("${security.jwt.secret}")
            String secret
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    // =========================================================
    // EXTRACT ALL CLAIMS
    // =========================================================

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    public String extractUsername(
            String token
    ) {

        return extractAllClaims(token)
                .getSubject();
    }

    // =========================================================
    // EXTRACT USER ID
    // =========================================================

    public Long extractUserId(
            String token
    ) {

        Object userId =
                extractAllClaims(token)
                        .get("user_id");

        if (userId == null) {

            throw new IllegalStateException(
                    "userId claim not found in JWT"
            );
        }

        return Long.valueOf(
                userId.toString()
        );
    }

    // =========================================================
    // EXTRACT ROLES
    // =========================================================

    public List<String> extractRoles(
            String token
    ) {

        Object roles =
                extractAllClaims(token)
                        .get("roles");

        if (roles instanceof List<?> list) {

            return list.stream()
                    .map(Object::toString)
                    .toList();
        }

        return Collections.emptyList();
    }

    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(
            String token
    ) {

        try {

            extractAllClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }
}