package com.platform.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProtectedController {

    @GetMapping("/api/v1//protected")
    public Map<String, Object> protectedApi(
            Authentication authentication
    ) {

        return Map.of(
                "message", "JWT authentication successful",
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}