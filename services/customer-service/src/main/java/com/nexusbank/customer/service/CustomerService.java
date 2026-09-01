package com.nexusbank.customer.service;

import com.nexusbank.customer.dto.request.CreateCustomerRequest;
import com.nexusbank.customer.dto.request.UpdateCustomerRequest;
import com.nexusbank.customer.dto.response.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomerById(UUID id);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request);

    CustomerResponse deactivateCustomer(UUID id);
}