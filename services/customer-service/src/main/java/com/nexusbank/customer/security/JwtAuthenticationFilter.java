package com.nexusbank.customer.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);

            String subject = claims.getSubject();

            if (subject == null || subject.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    subject,
                    null,
                    authorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(
            Claims claims) {

        Object rolesClaim = claims.get("roles");

        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private String normalizeRole(String role) {

        if (role.startsWith("ROLE_")) {
            return role;
        }

        return "ROLE_" + role;
    }
}