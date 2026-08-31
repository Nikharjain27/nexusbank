package com.nexusbank.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusbank.auth.dto.common.ApiErrorResponse;
import com.nexusbank.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.time.OffsetDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        ObjectMapper objectMapper) throws Exception {

                return http

                                .csrf(AbstractHttpConfigurer::disable)

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exceptionHandling -> exceptionHandling

                                                .authenticationEntryPoint(
                                                                (request, response, exception) -> {

                                                                        ApiErrorResponse error = new ApiErrorResponse(
                                                                                        OffsetDateTime.now(),
                                                                                        HttpServletResponse.SC_UNAUTHORIZED,
                                                                                        "Unauthorized",
                                                                                        "Authentication required",
                                                                                        request.getRequestURI());

                                                                        response.setStatus(
                                                                                        HttpServletResponse.SC_UNAUTHORIZED);
                                                                        response.setContentType("application/json");
                                                                        objectMapper.writeValue(
                                                                                        response.getOutputStream(),
                                                                                        error);
                                                                })

                                                .accessDeniedHandler(
                                                                (request, response, exception) -> {

                                                                        ApiErrorResponse error = new ApiErrorResponse(
                                                                                        OffsetDateTime.now(),
                                                                                        HttpServletResponse.SC_FORBIDDEN,
                                                                                        "Forbidden",
                                                                                        "Access denied",
                                                                                        request.getRequestURI());

                                                                        response.setStatus(
                                                                                        HttpServletResponse.SC_FORBIDDEN);
                                                                        response.setContentType("application/json");
                                                                        objectMapper.writeValue(
                                                                                        response.getOutputStream(),
                                                                                        error);
                                                                }))

                                .authorizeHttpRequests(authorize -> authorize

                                                .requestMatchers(
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/error")
                                                .permitAll()

                                                .requestMatchers("/api/auth/**")
                                                .permitAll()

                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/customer/**")
                                                .hasRole("CUSTOMER")

                                                .anyRequest()
                                                .authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                .build();
        }
}