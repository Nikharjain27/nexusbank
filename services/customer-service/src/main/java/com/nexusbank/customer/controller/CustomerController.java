package com.nexusbank.customer.controller;

import com.nexusbank.common.api.ApiResponse;
import com.nexusbank.common.constants.ApiConstants;
import com.nexusbank.customer.dto.request.CreateCustomerRequest;
import com.nexusbank.customer.dto.request.UpdateCustomerRequest;
import com.nexusbank.customer.dto.response.CustomerResponse;
import com.nexusbank.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            @RequestHeader(value = ApiConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Customer created successfully",
                        response,
                        correlationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable UUID id,
            @RequestHeader(value = ApiConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        CustomerResponse response = customerService.getCustomerById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        correlationId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestHeader(value = ApiConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        List<CustomerResponse> response = customerService.getAllCustomers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        correlationId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request,
            @RequestHeader(value = ApiConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        CustomerResponse response = customerService.updateCustomer(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer updated successfully",
                        response,
                        correlationId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CustomerResponse>> deactivateCustomer(
            @PathVariable UUID id,
            @RequestHeader(value = ApiConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        CustomerResponse response = customerService.deactivateCustomer(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer deactivated successfully",
                        response,
                        correlationId));
    }
}