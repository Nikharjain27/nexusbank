package com.nexusbank.customer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusbank.common.api.ErrorResponse;
import com.nexusbank.common.constants.ApiConstants;
import com.nexusbank.customer.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
                                (request, response, exception) -> writeErrorResponse(
                                        request,
                                        response,
                                        objectMapper,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "UNAUTHORIZED",
                                        "Authentication required"))

                        .accessDeniedHandler(
                                (request, response, exception) -> writeErrorResponse(
                                        request,
                                        response,
                                        objectMapper,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "FORBIDDEN",
                                        "Access denied")))

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error")
                        .permitAll()

                        .requestMatchers(
                                "POST",
                                "/api/customers")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(
                                "GET",
                                "/api/customers")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "GET",
                                "/api/customers/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(
                                "PUT",
                                "/api/customers/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(
                                "PATCH",
                                "/api/customers/**/deactivate")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String error,
            String message) throws java.io.IOException {

        String correlationId = request.getHeader(
                ApiConstants.CORRELATION_ID_HEADER);

        ErrorResponse errorResponse = ErrorResponse.of(
                status,
                error,
                message,
                request.getRequestURI(),
                correlationId);

        response.setStatus(status);
        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse);
    }
}
