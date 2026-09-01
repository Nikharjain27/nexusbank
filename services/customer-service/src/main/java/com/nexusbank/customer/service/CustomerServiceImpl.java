package com.nexusbank.customer.service;

import com.nexusbank.common.exception.ResourceNotFoundException;
import com.nexusbank.customer.domain.entity.Customer;
import com.nexusbank.customer.domain.enums.CustomerStatus;
import com.nexusbank.customer.dto.request.CreateCustomerRequest;
import com.nexusbank.customer.dto.request.UpdateCustomerRequest;
import com.nexusbank.customer.dto.response.CustomerResponse;
import com.nexusbank.customer.exception.CustomerBusinessException;
import com.nexusbank.customer.mapper.CustomerMapper;
import com.nexusbank.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        String email = request.email().trim();

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new CustomerBusinessException(
                    "CUSTOMER_EMAIL_ALREADY_EXISTS",
                    "Customer with email already exists");
        }

        String customerNumber = generateCustomerNumber();

        Customer customer = customerMapper.toEntity(
                request,
                customerNumber);

        customer.setEmail(email);

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer",
                        id));

        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(
            UUID id,
            UpdateCustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer",
                        id));

        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new CustomerBusinessException(
                    "CUSTOMER_INACTIVE_UPDATE",
                    "Inactive customer cannot be updated");
        }

        String email = request.email().trim();

        customerRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new CustomerBusinessException(
                            "CUSTOMER_EMAIL_ALREADY_EXISTS",
                            "Customer with email already exists");
                });

        customerMapper.updateEntity(customer, request);
        customer.setEmail(email);

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse deactivateCustomer(UUID id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer",
                        id));

        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new CustomerBusinessException(
                    "CUSTOMER_ALREADY_INACTIVE",
                    "Customer is already inactive");
        }

        customer.setStatus(CustomerStatus.INACTIVE);

        return customerMapper.toResponse(customer);
    }

    private String generateCustomerNumber() {

        String customerNumber;

        do {
            customerNumber = "CUST"
                    + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 12)
                            .toUpperCase();

        } while (customerRepository.existsByCustomerNumber(customerNumber));

        return customerNumber;
    }
}