package com.nexusbank.common.api;

import com.nexusbank.common.constants.ApiConstants;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        String correlationId
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            String correlationId
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                Instant.now(),
                correlationId
        );
    }

    public static <T> ApiResponse<T> success(
            T data,
            String correlationId
    ) {
        return success(
                ApiConstants.DEFAULT_SUCCESS_MESSAGE,
                data,
                correlationId
        );
    }

    public static <T> ApiResponse<T> failure(
            String message,
            T data,
            String correlationId
    ) {
        return new ApiResponse<>(
                false,
                message,
                data,
                Instant.now(),
                correlationId
        );
    }
}