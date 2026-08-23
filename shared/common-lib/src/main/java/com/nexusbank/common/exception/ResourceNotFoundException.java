package com.nexusbank.common.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(
            String errorCode,
            String message
    ) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(
            String resourceName,
            Object resourceId
    ) {
        super(
                resourceName.toUpperCase() + "_NOT_FOUND",
                resourceName + " with identifier " + resourceId + " was not found"
        );
    }
}