package com.platform.auth.security;

import com.platform.auth.config.JwtProperties;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        Instant expiresAt = now.plusSeconds(
                jwtProperties.getAccessTokenExpirationMinutes() * 60L
        );

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getCode)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("user_id", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();
    }

    public boolean isTokenValid(String token) {

        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {

        Jwt jwt = jwtDecoder.decode(token);

        return jwt.getSubject();
    }

    public Long extractUserId(String token) {

        Jwt jwt = jwtDecoder.decode(token);

        Object value = jwt.getClaim("user_id");

        if (value == null) {
            return null;
        }

        return ((Number) value).longValue();
    }

    public String extractEmail(String token) {

        Jwt jwt = jwtDecoder.decode(token);

        return jwt.getClaimAsString("email");
    }

    public List<String> extractRoles(String token) {

        Jwt jwt = jwtDecoder.decode(token);

        List<String> roles =
                jwt.getClaimAsStringList("roles");

        return roles != null
                ? roles
                : List.of();
    }
}