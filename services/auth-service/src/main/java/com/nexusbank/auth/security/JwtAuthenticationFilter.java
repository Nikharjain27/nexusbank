package com.nexusbank.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final NexusBankUserDetailsService userDetailsService;

        public JwtAuthenticationFilter(
                        JwtService jwtService,
                        NexusBankUserDetailsService userDetailsService) {
                this.jwtService = jwtService;
                this.userDetailsService = userDetailsService;
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

                        UUID publicId = UUID.fromString(subject);

                        NexusBankUserDetails userDetails = userDetailsService.loadUserByPublicId(publicId);

                        if (SecurityContextHolder.getContext()
                                        .getAuthentication() == null) {

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities());

                                SecurityContextHolder.getContext()
                                                .setAuthentication(authentication);
                        }

                } catch (Exception exception) {

                        SecurityContextHolder.clearContext();

                        logger.debug(
                                        "JWT authentication failed",
                                        exception);
                }

                filterChain.doFilter(request, response);
        }
}