package com.nexusbank.customer.security;

import com.nexusbank.customer.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret()
                .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
