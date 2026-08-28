package com.nexusbank.auth.service;

import com.nexusbank.auth.config.JwtProperties;
import com.nexusbank.auth.domain.entity.RefreshToken;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.repository.RefreshTokenRepository;
import com.nexusbank.auth.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@Transactional
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final SecureRandom secureRandom;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.jwtService = jwtService;
        this.secureRandom = new SecureRandom();
    }

    public String createRefreshToken(User user) {

        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plus(jwtProperties.getRefreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                expiresAt);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public LoginTokenResult refresh(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken existingToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existingToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (existingToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = existingToken.getUser();

        if (!user.isEnabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        if (!user.isAccountNonLocked()) {
            throw new IllegalStateException("User account is locked");
        }

        existingToken.revoke();

        String newAccessToken = jwtService.generateAccessToken(user);

        String newRefreshToken = createRefreshToken(user);

        return new LoginTokenResult(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTokenExpiration().toSeconds());
    }

    public void logout(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
        }
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception);
        }
    }

    public record LoginTokenResult(
            String accessToken,
            String refreshToken,
            long expiresIn) {
    }

    public void revokeRefreshToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        refreshToken.revoke();
    }
}