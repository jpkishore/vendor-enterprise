package com.platform.auth.dto.auth;

public record RegisterResponse(
        Long userId,
        String username,
        String email,
        String message
) {
}