package com.nexusbank.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        Instant timestamp,
        List<FieldError> fieldErrors
) {

    public static ErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            String correlationId
    ) {
        return new ErrorResponse(
                false,
                status,
                error,
                message,
                path,
                correlationId,
                Instant.now(),
                List.of()
        );
    }

    public static ErrorResponse validationFailure(
            int status,
            String error,
            String message,
            String path,
            String correlationId,
            List<FieldError> fieldErrors
    ) {
        return new ErrorResponse(
                false,
                status,
                error,
                message,
                path,
                correlationId,
                Instant.now(),
                fieldErrors == null ? List.of() : List.copyOf(fieldErrors)
        );
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}