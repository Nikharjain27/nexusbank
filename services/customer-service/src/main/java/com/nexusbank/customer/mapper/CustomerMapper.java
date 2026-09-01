package com.nexusbank.customer.mapper;

import com.nexusbank.customer.domain.entity.Customer;
import com.nexusbank.customer.domain.enums.CustomerStatus;
import com.nexusbank.customer.dto.request.CreateCustomerRequest;
import com.nexusbank.customer.dto.request.UpdateCustomerRequest;
import com.nexusbank.customer.dto.response.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(
            CreateCustomerRequest request,
            String customerNumber) {
        Customer customer = new Customer();

        customer.setCustomerNumber(customerNumber);
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setAddress(request.address());
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }

    public void updateEntity(
            Customer customer,
            UpdateCustomerRequest request) {
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDateOfBirth(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}