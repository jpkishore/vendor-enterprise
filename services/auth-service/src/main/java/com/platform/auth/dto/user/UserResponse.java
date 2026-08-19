package com.platform.auth.dto.user;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean accountEnabled,
        boolean accountLocked,
        List<String> roles
) {
}