package com.platform.catalog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
            @Value("${security.jwt.secret}")
            String secret
    ) {

        this.secretKey =
                new SecretKeySpec(
                        secret.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                );
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {

        try {

            extractAllClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public List<String> extractRoles(String token) {

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
}