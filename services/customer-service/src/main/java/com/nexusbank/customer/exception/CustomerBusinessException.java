package com.nexusbank.customer.exception;

import com.nexusbank.common.exception.BusinessException;

public class CustomerBusinessException extends BusinessException {

    public CustomerBusinessException(
            String errorCode,
            String message) {
        super(errorCode, message);
    }
}