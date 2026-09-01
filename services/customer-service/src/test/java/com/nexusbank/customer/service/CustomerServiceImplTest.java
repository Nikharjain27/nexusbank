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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private UUID customerId;
    private Customer customer;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {

        customerId = UUID.randomUUID();

        customer = new Customer();

        customer.setFirstName("Nikhar");
        customer.setLastName("Jain");
        customer.setCustomerNumber("CUST123456789");
        customer.setEmail("nikhar@example.com");
        customer.setPhone("+919876543210");
        customer.setDateOfBirth(LocalDate.of(1999, 1, 1));
        customer.setAddress("Indore");
        customer.setStatus(CustomerStatus.ACTIVE);

        customerResponse = new CustomerResponse(
                customerId,
                "CUST123456789",
                "Nikhar",
                "Jain",
                "nikhar@example.com",
                "+919876543210",
                LocalDate.of(1999, 1, 1),
                "Indore",
                CustomerStatus.ACTIVE,
                null,
                null);
    }

    @Test
    void shouldCreateCustomer() {

        CreateCustomerRequest request = new CreateCustomerRequest(
                "Nikhar",
                "Jain",
                "nikhar@example.com",
                "+919876543210",
                LocalDate.of(1999, 1, 1),
                "Indore");

        when(customerRepository.existsByEmailIgnoreCase(
                "nikhar@example.com")).thenReturn(false);

        when(customerMapper.toEntity(any(), any()))
                .thenReturn(customer);

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        CustomerResponse result = customerService.createCustomer(request);

        assertNotNull(result);

        assertEquals(
                "CUST123456789",
                result.customerNumber());

        assertEquals(
                CustomerStatus.ACTIVE,
                result.status());

        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectDuplicateEmail() {

        CreateCustomerRequest request = new CreateCustomerRequest(
                "Nikhar",
                "Jain",
                "nikhar@example.com",
                "+919876543210",
                LocalDate.of(1999, 1, 1),
                "Indore");

        when(customerRepository.existsByEmailIgnoreCase(
                "nikhar@example.com")).thenReturn(true);

        CustomerBusinessException exception = assertThrows(
                CustomerBusinessException.class,
                () -> customerService.createCustomer(request));

        assertEquals(
                "CUSTOMER_EMAIL_ALREADY_EXISTS",
                exception.getErrorCode());

        assertEquals(
                "Customer with email already exists",
                exception.getMessage());

        verify(customerRepository, never())
                .save(any());
    }

    @Test
    void shouldGetCustomerById() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        CustomerResponse result = customerService.getCustomerById(customerId);

        assertNotNull(result);

        assertEquals(
                customerId,
                result.id());

        assertEquals(
                "nikhar@example.com",
                result.email());

        verify(customerRepository)
                .findById(customerId);

        verify(customerMapper)
                .toResponse(customer);
    }

    @Test
    void shouldThrowResourceNotFoundWhenCustomerDoesNotExist() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomerById(customerId));

        assertEquals(
                "CUSTOMER_NOT_FOUND",
                exception.getErrorCode());

        assertEquals(
                "Customer with identifier " + customerId
                        + " was not found",
                exception.getMessage());

        verify(customerRepository)
                .findById(customerId);

        verifyNoInteractions(customerMapper);
    }

    @Test
    void shouldGetAllCustomers() {

        Customer secondCustomer = new Customer();

        UUID secondCustomerId = UUID.randomUUID();

        secondCustomer.setFirstName("Test");
        secondCustomer.setLastName("Customer");
        secondCustomer.setCustomerNumber("CUST987654321");
        secondCustomer.setEmail("test@example.com");
        secondCustomer.setPhone("+919999999999");
        secondCustomer.setDateOfBirth(
                LocalDate.of(1998, 1, 1));
        secondCustomer.setAddress("Bhopal");
        secondCustomer.setStatus(CustomerStatus.ACTIVE);

        CustomerResponse secondResponse = new CustomerResponse(
                secondCustomerId,
                "CUST987654321",
                "Test",
                "Customer",
                "test@example.com",
                "+919999999999",
                LocalDate.of(1998, 1, 1),
                "Bhopal",
                CustomerStatus.ACTIVE,
                null,
                null);

        when(customerRepository.findAll())
                .thenReturn(List.of(
                        customer,
                        secondCustomer));

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        when(customerMapper.toResponse(secondCustomer))
                .thenReturn(secondResponse);

        List<CustomerResponse> result = customerService.getAllCustomers();

        assertNotNull(result);

        assertEquals(
                2,
                result.size());

        assertEquals(
                "CUST123456789",
                result.get(0).customerNumber());

        assertEquals(
                "CUST987654321",
                result.get(1).customerNumber());

        verify(customerRepository)
                .findAll();

        verify(customerMapper)
                .toResponse(customer);

        verify(customerMapper)
                .toResponse(secondCustomer);
    }

    @Test
    void shouldUpdateCustomer() {

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated",
                "Jain",
                "updated@example.com",
                "+919876543211",
                "Updated Address");

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmailIgnoreCase(
                "updated@example.com")).thenReturn(Optional.empty());

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        CustomerResponse result = customerService.updateCustomer(
                customerId,
                request);

        assertNotNull(result);

        verify(customerRepository)
                .findById(customerId);

        verify(customerRepository)
                .findByEmailIgnoreCase(
                        "updated@example.com");

        verify(customerMapper)
                .updateEntity(customer, request);

        verify(customerMapper)
                .toResponse(customer);
    }

    @Test
    void shouldRejectUpdateForInactiveCustomer() {

        customer.setStatus(CustomerStatus.INACTIVE);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated",
                "Jain",
                "updated@example.com",
                "+919876543211",
                "Updated Address");

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        CustomerBusinessException exception = assertThrows(
                CustomerBusinessException.class,
                () -> customerService.updateCustomer(
                        customerId,
                        request));

        assertEquals(
                "CUSTOMER_INACTIVE_UPDATE",
                exception.getErrorCode());

        assertEquals(
                "Inactive customer cannot be updated",
                exception.getMessage());

        verify(customerRepository)
                .findById(customerId);

        verify(customerRepository, never())
                .findByEmailIgnoreCase(any());

        verify(customerMapper, never())
                .updateEntity(any(), any());
    }

    @Test
    void shouldRejectDuplicateEmailDuringUpdate() {

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated",
                "Jain",
                "existing@example.com",
                "+919876543211",
                "Updated Address");

        Customer existingCustomer = new Customer();

        UUID existingCustomerId = UUID.randomUUID();

        existingCustomer.setFirstName("Existing");
        existingCustomer.setLastName("Customer");
        existingCustomer.setCustomerNumber("CUST999999999");
        existingCustomer.setEmail("existing@example.com");
        existingCustomer.setStatus(CustomerStatus.ACTIVE);

        /*
         * Important:
         * The existing customer must have an ID because
         * CustomerServiceImpl compares it with the customer
         * being updated.
         */
        setCustomerId(existingCustomer, existingCustomerId);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmailIgnoreCase(
                "existing@example.com")).thenReturn(Optional.of(existingCustomer));

        CustomerBusinessException exception = assertThrows(
                CustomerBusinessException.class,
                () -> customerService.updateCustomer(
                        customerId,
                        request));

        assertEquals(
                "CUSTOMER_EMAIL_ALREADY_EXISTS",
                exception.getErrorCode());

        assertEquals(
                "Customer with email already exists",
                exception.getMessage());

        verify(customerMapper, never())
                .updateEntity(any(), any());
    }

    @Test
    void shouldAllowCustomerToKeepOwnEmailDuringUpdate() {

        /*
         * The test customer represents a persisted entity,
         * therefore it must have the same ID used in the lookup.
         */
        setCustomerId(customer, customerId);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated",
                "Jain",
                "nikhar@example.com",
                "+919876543211",
                "Updated Address");

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmailIgnoreCase(
                "nikhar@example.com")).thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        CustomerResponse result = customerService.updateCustomer(
                customerId,
                request);

        assertNotNull(result);

        verify(customerMapper)
                .updateEntity(customer, request);
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingMissingCustomer() {

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated",
                "Jain",
                "updated@example.com",
                "+919876543211",
                "Updated Address");

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.updateCustomer(
                        customerId,
                        request));

        assertEquals(
                "CUSTOMER_NOT_FOUND",
                exception.getErrorCode());

        verify(customerMapper, never())
                .updateEntity(any(), any());
    }

    @Test
    void shouldDeactivateCustomer() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer))
                .thenReturn(customerResponse);

        CustomerResponse result = customerService.deactivateCustomer(customerId);

        assertNotNull(result);

        assertEquals(
                CustomerStatus.INACTIVE,
                customer.getStatus());

        verify(customerRepository)
                .findById(customerId);

        verify(customerMapper)
                .toResponse(customer);
    }

    @Test
    void shouldRejectAlreadyInactiveCustomer() {

        customer.setStatus(CustomerStatus.INACTIVE);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        CustomerBusinessException exception = assertThrows(
                CustomerBusinessException.class,
                () -> customerService.deactivateCustomer(
                        customerId));

        assertEquals(
                "CUSTOMER_ALREADY_INACTIVE",
                exception.getErrorCode());

        assertEquals(
                "Customer is already inactive",
                exception.getMessage());

        verify(customerRepository)
                .findById(customerId);

        verify(customerMapper, never())
                .toResponse(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeactivatingMissingCustomer() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.deactivateCustomer(
                        customerId));

        assertEquals(
                "CUSTOMER_NOT_FOUND",
                exception.getErrorCode());

        verify(customerRepository)
                .findById(customerId);

        verify(customerMapper, never())
                .toResponse(any());
    }

    /**
     * Helper method used only by tests to assign a UUID to the
     * private generated ID field of Customer.
     *
     * In production, the ID is generated by JPA/Hibernate.
     */
    private void setCustomerId(
            Customer customer,
            UUID id) {
        try {
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(customer, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Unable to assign customer ID for test",
                    exception);
        }
    }
}
