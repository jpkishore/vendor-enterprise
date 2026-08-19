package com.platform.auth.controller;

import com.platform.auth.dto.auth.LoginResponse;
import com.platform.auth.dto.auth.RegisterRequest;
import com.platform.auth.dto.auth.RegisterResponse;
import com.platform.auth.security.JwtService;
import com.platform.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;
    @Test
    void shouldRegisterUser() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210"
        );

        RegisterResponse response = new RegisterResponse(
                1L,
                "kishore",
                "kishore@example.com",
                "User registered successfully"
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("kishore"))
                .andExpect(jsonPath("$.email")
                        .value("kishore@example.com"));
    }

    @Test
    void shouldRefreshTokenSuccessfully()
            throws Exception {

        LoginResponse response =
                new LoginResponse(
                        "new-access-token",
                        "new-refresh-token",
                        "Bearer",
                        900,
                        1L,
                        "kishore",
                        "kishore@example.com",
                        List.of("CUSTOMER")
                );

        when(authService.refresh("old-refresh-token"))
                .thenReturn(response);

        String requestJson = """
            {
                "refreshToken": "old-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accessToken")
                                .value("new-access-token")
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("new-refresh-token")
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresIn")
                                .value(900)
                );
    }
}