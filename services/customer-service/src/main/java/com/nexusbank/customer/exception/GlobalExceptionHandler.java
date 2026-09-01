package com.nexusbank.customer.exception;

import com.nexusbank.common.api.ErrorResponse;
import com.nexusbank.common.constants.ApiConstants;
import com.nexusbank.common.exception.BaseException;
import com.nexusbank.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ErrorResponse response = ErrorResponse.validationFailure(
                HttpStatus.BAD_REQUEST.value(),
                ApiConstants.VALIDATION_ERROR,
                "Request validation failed",
                request.getRequestURI(),
                getCorrelationId(request),
                fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        ErrorResponse response = ErrorResponse.validationFailure(
                HttpStatus.BAD_REQUEST.value(),
                ApiConstants.VALIDATION_ERROR,
                "Request validation failed",
                request.getRequestURI(),
                getCorrelationId(request),
                fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(CustomerBusinessException.class)
    public ResponseEntity<ErrorResponse> handleCustomerBusinessException(
            CustomerBusinessException exception,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request));

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException exception,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request));

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ApiConstants.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                getCorrelationId(request));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private String getCorrelationId(HttpServletRequest request) {

        return request.getHeader(
                ApiConstants.CORRELATION_ID_HEADER);
    }
}