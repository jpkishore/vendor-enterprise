package com.platform.auth.dto.auth;

import java.util.List;

public record LoginResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn,

        Long userId,

        String username,

        String email,

        List<String> roles
) {
}