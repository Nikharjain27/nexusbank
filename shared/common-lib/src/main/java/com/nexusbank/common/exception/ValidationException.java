package com.nexusbank.common.exception;

public class ValidationException extends BaseException {

    public ValidationException(
            String errorCode,
            String message
    ) {
        super(errorCode, message);
    }

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}