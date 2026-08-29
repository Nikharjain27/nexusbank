package com.nexusbank.auth.dto.common;

import java.time.OffsetDateTime;

public record ApiErrorResponse(

        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path

) {
}