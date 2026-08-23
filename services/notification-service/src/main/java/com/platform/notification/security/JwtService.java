package com.platform.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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
                        secret.getBytes()
                );
    }

    public Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(
            String token
    ) {

        return extractAllClaims(token)
                .getSubject();
    }

    public Long extractUserId(
            String token
    ) {

        Object userId =
                extractAllClaims(token)
                        .get("user_id");

        if (userId == null) {

            throw new IllegalStateException(
                    "user_id claim not found in JWT"
            );
        }

        return Long.valueOf(
                userId.toString()
        );
    }

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

        return List.of();
    }

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