package com.nexusbank.common.constants;

public final class ApiConstants {

    private ApiConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String API_VERSION_V1 = "/api/v1";

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String DEFAULT_SUCCESS_MESSAGE =
            "Request completed successfully";

    public static final String VALIDATION_ERROR =
            "VALIDATION_ERROR";

    public static final String INTERNAL_SERVER_ERROR =
            "INTERNAL_SERVER_ERROR";
}