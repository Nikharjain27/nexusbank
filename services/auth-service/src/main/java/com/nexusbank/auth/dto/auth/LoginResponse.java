package com.nexusbank.auth.dto.auth;

public record LoginResponse(

        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn

) {
}