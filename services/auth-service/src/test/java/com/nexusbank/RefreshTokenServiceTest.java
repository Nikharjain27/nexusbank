package com.nexusbank;

import com.nexusbank.auth.config.JwtProperties;
import com.nexusbank.auth.domain.entity.RefreshToken;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.repository.RefreshTokenRepository;
import com.nexusbank.auth.security.JwtService;
import com.nexusbank.auth.service.RefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtService jwtService;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                jwtProperties,
                jwtService);

        user = new User(
                "customer@nexusbank.com",
                "encoded-password",
                "John",
                "Doe");
    }

    @Test
    void createRefreshTokenShouldGenerateAndPersistToken() {

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(Duration.ofDays(7));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        String rawToken = refreshTokenService.createRefreshToken(user);

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        verify(refreshTokenRepository)
                .save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertNotNull(savedToken);
        assertSame(user, savedToken.getUser());
        assertNotNull(savedToken.getTokenHash());
        assertFalse(savedToken.getTokenHash().isBlank());
        assertNotEquals(rawToken, savedToken.getTokenHash());

        assertNotNull(savedToken.getExpiresAt());
        assertFalse(savedToken.isRevoked());
    }

    @Test
    void createRefreshTokenShouldGenerateDifferentTokens() {

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(Duration.ofDays(7));

        String firstToken = refreshTokenService.createRefreshToken(user);

        String secondToken = refreshTokenService.createRefreshToken(user);

        assertNotNull(firstToken);
        assertNotNull(secondToken);
        assertNotEquals(firstToken, secondToken);

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void refreshShouldRejectBlankToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refresh(" "));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void refreshShouldRejectNullToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refresh(null));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void refreshShouldRejectInvalidToken() {

        String rawToken = "invalid-refresh-token";

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refresh(rawToken));

        assertEquals(
                "Invalid refresh token",
                exception.getMessage());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }

    @Test
    void refreshShouldRejectRevokedToken() {

        String rawToken = "revoked-refresh-token";

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().plusDays(7));

        refreshToken.revoke();

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(refreshToken));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refresh(rawToken));

        assertEquals(
                "Refresh token has been revoked",
                exception.getMessage());

        verify(refreshTokenRepository)
                .findByTokenHash(any());

        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshShouldRejectExpiredToken() {

        String rawToken = "expired-refresh-token";

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().minusMinutes(1));

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(refreshToken));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.refresh(rawToken));

        assertEquals(
                "Refresh token has expired",
                exception.getMessage());

        verify(refreshTokenRepository)
                .findByTokenHash(any());

        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshShouldRotateTokensForValidToken() {

        String rawToken = "valid-refresh-token";

        RefreshToken existingToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(existingToken));

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(jwtService.getAccessTokenExpiration())
                .thenReturn(Duration.ofMinutes(15));

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(Duration.ofDays(7));

        RefreshTokenService.LoginTokenResult result = refreshTokenService.refresh(rawToken);

        assertNotNull(result);
        assertEquals(
                "new-access-token",
                result.accessToken());

        assertNotNull(result.refreshToken());
        assertFalse(result.refreshToken().isBlank());

        assertEquals(
                Duration.ofMinutes(15).toSeconds(),
                result.expiresIn());

        assertTrue(existingToken.isRevoked());
        assertNotNull(existingToken.getRevokedAt());

        verify(refreshTokenRepository)
                .findByTokenHash(any());

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void logoutShouldRejectBlankToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.logout(" "));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void logoutShouldRejectNullToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.logout(null));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void logoutShouldRejectInvalidToken() {

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.logout("invalid-token"));

        assertEquals(
                "Invalid refresh token",
                exception.getMessage());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }

    @Test
    void logoutShouldRevokeActiveToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(refreshToken));

        assertFalse(refreshToken.isRevoked());

        refreshTokenService.logout("valid-token");

        assertTrue(refreshToken.isRevoked());
        assertNotNull(refreshToken.getRevokedAt());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }

    @Test
    void logoutShouldNotChangeAlreadyRevokedToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().plusDays(7));

        refreshToken.revoke();

        OffsetDateTime originalRevokedAt = refreshToken.getRevokedAt();

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.logout("already-revoked-token");

        assertTrue(refreshToken.isRevoked());

        assertEquals(
                originalRevokedAt,
                refreshToken.getRevokedAt());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }

    @Test
    void revokeRefreshTokenShouldRejectBlankToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.revokeRefreshToken(" "));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void revokeRefreshTokenShouldRejectNullToken() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.revokeRefreshToken(null));

        assertEquals(
                "Refresh token is required",
                exception.getMessage());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void revokeRefreshTokenShouldRejectInvalidToken() {

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.revokeRefreshToken(
                        "invalid-token"));

        assertEquals(
                "Invalid refresh token",
                exception.getMessage());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }

    @Test
    void revokeRefreshTokenShouldRevokeToken() {

        RefreshToken refreshToken = new RefreshToken(
                user,
                "hashed-token",
                OffsetDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeRefreshToken("valid-token");

        assertTrue(refreshToken.isRevoked());
        assertNotNull(refreshToken.getRevokedAt());

        verify(refreshTokenRepository)
                .findByTokenHash(any());
    }
}