package com.nexusbank.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusbank.auth.dto.auth.LoginRequest;
import com.nexusbank.auth.dto.auth.LoginResponse;
import com.nexusbank.auth.dto.auth.RefreshTokenRequest;
import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;
import com.nexusbank.auth.security.JwtService;
import com.nexusbank.auth.security.NexusBankUserDetailsService;
import com.nexusbank.auth.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.nexusbank.auth.exception.GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private NexusBankUserDetailsService userDetailsService;

    @Test
    void registerShouldReturn201()
            throws Exception {

        UUID userId = UUID.randomUUID();

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("nikhar@example.com");
        request.setPassword("Password123");

        RegisterResponse response = new RegisterResponse(
                userId,
                "nikhar@example.com",
                "Nikhar",
                "Jain",
                "User registered successfully");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("nikhar@example.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("Nikhar"))
                .andExpect(jsonPath("$.lastName")
                        .value("Jain"))
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        verify(authService)
                .register(any(RegisterRequest.class));
    }

    @Test
    void registerWithInvalidEmailShouldReturn400()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("invalid-email");
        request.setPassword("Password123");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "email: Email must be valid")))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/register"));
    }

    @Test
    void registerWithMissingRequiredFieldsShouldReturn400()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/register"));
    }

    @Test
    void registerWithShortPasswordShouldReturn400()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("nikhar@example.com");
        request.setPassword("123");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "password: Password must be between 8 and 100 characters")))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/register"));
    }

    @Test
    void registerWhenEmailAlreadyExistsShouldReturn400()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("nikhar@example.com");
        request.setPassword("Password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Email is already registered"));

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/register"));
    }

    @Test
    void loginShouldReturn200()
            throws Exception {

        LoginRequest request = new LoginRequest(
                "nikhar@example.com",
                "Password123");

        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900);

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900));

        verify(authService)
                .login(any(LoginRequest.class));
    }

    @Test
    void loginWithInvalidCredentialsShouldReturn400()
            throws Exception {

        LoginRequest request = new LoginRequest(
                "nikhar@example.com",
                "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid email or password"));

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/login"));
    }

    @Test
    void loginWithInvalidEmailShouldReturn400()
            throws Exception {

        LoginRequest request = new LoginRequest(
                "invalid-email",
                "Password123");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/login"));
    }

    @Test
    void refreshShouldReturn200() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        LoginResponse response = new LoginResponse(
                "new-access-token",
                "new-refresh-token",
                "Bearer",
                900);

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900));

        verify(authService)
                .refresh(any(RefreshTokenRequest.class));
    }

    @Test
    void refreshWithInvalidTokenShouldReturn400() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid refresh token"));

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid refresh token"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/refresh"));

        verify(authService)
                .refresh(any(RefreshTokenRequest.class));
    }

    @Test
    void refreshWithBlankTokenShouldReturn400()
            throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/refresh"));
    }

    @Test
    void logoutShouldReturn204() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(
                post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService)
                .logout(any(RefreshTokenRequest.class));
    }

    @Test
    void logoutWithInvalidTokenShouldReturn400() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

        doThrow(
                new IllegalArgumentException(
                        "Invalid refresh token"))
                .when(authService)
                .logout(any(RefreshTokenRequest.class));

        mockMvc.perform(
                post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid refresh token"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/logout"));

        verify(authService)
                .logout(any(RefreshTokenRequest.class));
    }

    @Test
    void logoutWithBlankTokenShouldReturn400()
            throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(
                post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/logout"));
    }
}