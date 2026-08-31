package com.nexusbank.auth.security;

import com.nexusbank.auth.config.JwtProperties;
import com.nexusbank.auth.domain.entity.Role;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.security.JwtService;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "this-is-a-long-random-secret-key-for-jwt-testing-only-123456789";
    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtService = new JwtService(jwtProperties);
        user = new User("customer@nexusbank.com", "encoded-password", "John", "Doe");
        setPublicId(user, UUID.randomUUID());
        Role customerRole = new Role("ROLE_CUSTOMER", "Customer role");
        user.getRoles().add(customerRole);
    }

    private void setPublicId(User user, UUID publicId) throws Exception {
        Field field = User.class.getDeclaredField("publicId");
        field.setAccessible(true);
        field.set(user, publicId);
    }

    @Test
    void generateAccessTokenShouldCreateValidToken() {
        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());
        Claims claims = jwtService.extractClaims(token);
        assertNotNull(claims);
    }

    @Test
    void generatedTokenShouldContainUserPublicIdAsSubject() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);
        assertEquals(user.getPublicId().toString(), claims.getSubject());
    }

    @Test
    void generatedTokenShouldContainEmail() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);
        assertEquals(user.getEmail(), claims.get("email", String.class));
    }

    @Test
    void generatedTokenShouldContainRoles() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);
        Object roles = claims.get("roles");
        assertNotNull(roles);
        assertTrue(roles.toString().contains("ROLE_CUSTOMER"));
    }

    @Test
    void generatedTokenShouldHaveIssuedAt() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    void generatedTokenShouldHaveExpiration() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void generatedTokenShouldBeValid() {
        String token = jwtService.generateAccessToken(user);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void invalidTokenShouldNotBeValid() {
        String invalidToken = "this.is.not.a.valid.jwt";
        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void tamperedTokenShouldNotBeValid() {
        String token = jwtService.generateAccessToken(user);
        String tamperedToken = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        assertFalse(jwtService.isTokenValid(tamperedToken));
    }

    @Test
    void tokenSignedWithDifferentSecretShouldNotBeValid() {
        String token = jwtService.generateAccessToken(user);
        JwtProperties differentProperties = new JwtProperties();
        differentProperties.setSecret("another-long-random-secret-key-for-jwt-testing-987654321");
        differentProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        JwtService differentJwtService = new JwtService(differentProperties);
        assertFalse(differentJwtService.isTokenValid(token));
    }

    @Test
    void expiredTokenShouldNotBeValid() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(SECRET);
        expiredProperties.setAccessTokenExpiration(Duration.ofMillis(-1));
        JwtService expiredJwtService = new JwtService(expiredProperties);
        String token = expiredJwtService.generateAccessToken(user);
        assertFalse(expiredJwtService.isTokenValid(token));
    }

    @Test
    void getAccessTokenExpirationShouldReturnConfiguredDuration() {
        Duration expected = Duration.ofMinutes(15);
        assertEquals(expected, jwtService.getAccessTokenExpiration());
    }

    @Test
    void generatedTokensShouldBeValid() {
        String firstToken = jwtService.generateAccessToken(user);
        String secondToken = jwtService.generateAccessToken(user);
        assertTrue(jwtService.isTokenValid(firstToken));
        assertTrue(jwtService.isTokenValid(secondToken));
    }
}