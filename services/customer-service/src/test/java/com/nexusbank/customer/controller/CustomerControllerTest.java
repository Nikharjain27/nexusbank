package com.nexusbank.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusbank.common.exception.ResourceNotFoundException;
import com.nexusbank.customer.domain.enums.CustomerStatus;
import com.nexusbank.customer.dto.request.CreateCustomerRequest;
import com.nexusbank.customer.dto.request.UpdateCustomerRequest;
import com.nexusbank.customer.dto.response.CustomerResponse;
import com.nexusbank.customer.exception.CustomerBusinessException;
import com.nexusbank.customer.exception.GlobalExceptionHandler;
import com.nexusbank.customer.security.JwtAuthenticationFilter;
import com.nexusbank.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CustomerService customerService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private final UUID customerId = UUID.randomUUID();

        private CustomerResponse customerResponse() {

                return new CustomerResponse(
                                customerId,
                                "CUST123456789",
                                "Nikhar",
                                "Jain",
                                "nikhar@example.com",
                                "+919876543210",
                                LocalDate.of(1999, 1, 1),
                                "Indore",
                                CustomerStatus.ACTIVE,
                                OffsetDateTime.now(),
                                OffsetDateTime.now());
        }

        @Test
        void shouldCreateCustomer() throws Exception {

                CreateCustomerRequest request = new CreateCustomerRequest(
                                "Nikhar",
                                "Jain",
                                "nikhar@example.com",
                                "+919876543210",
                                LocalDate.of(1999, 1, 1),
                                "Indore");

                when(customerService.createCustomer(any(
                                CreateCustomerRequest.class))).thenReturn(customerResponse());

                mockMvc.perform(
                                post("/api/customers")
                                                .header(
                                                                "X-Correlation-Id",
                                                                "test-correlation-id")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath(
                                                "$.message").value("Customer created successfully"))
                                .andExpect(jsonPath(
                                                "$.data.customerNumber").value("CUST123456789"))
                                .andExpect(jsonPath(
                                                "$.data.email").value("nikhar@example.com"))
                                .andExpect(jsonPath(
                                                "$.data.status").value("ACTIVE"))
                                .andExpect(jsonPath(
                                                "$.correlationId").value("test-correlation-id"));
        }

        @Test
        void shouldRejectInvalidCreateRequest() throws Exception {

                CreateCustomerRequest request = new CreateCustomerRequest(
                                "",
                                "",
                                "invalid-email",
                                "123",
                                LocalDate.now().plusDays(1),
                                "");

                mockMvc.perform(
                                post("/api/customers")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath(
                                                "$.error").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath(
                                                "$.fieldErrors").isArray());
        }

        @Test
        void shouldGetCustomerById() throws Exception {

                when(customerService.getCustomerById(customerId))
                                .thenReturn(customerResponse());

                mockMvc.perform(
                                get("/api/customers/{id}", customerId)
                                                .header(
                                                                "X-Correlation-Id",
                                                                "get-correlation-id"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath(
                                                "$.data.id").value(customerId.toString()))
                                .andExpect(jsonPath(
                                                "$.data.customerNumber").value("CUST123456789"))
                                .andExpect(jsonPath(
                                                "$.correlationId").value("get-correlation-id"));
        }

        @Test
        void shouldReturnNotFoundWhenCustomerDoesNotExist()
                        throws Exception {

                when(customerService.getCustomerById(customerId))
                                .thenThrow(
                                                new ResourceNotFoundException(
                                                                "Customer",
                                                                customerId));

                mockMvc.perform(
                                get("/api/customers/{id}", customerId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath(
                                                "$.error").value("CUSTOMER_NOT_FOUND"))
                                .andExpect(jsonPath(
                                                "$.message").value(
                                                                "Customer with identifier "
                                                                                + customerId
                                                                                + " was not found"));
        }

        @Test
        void shouldGetAllCustomers() throws Exception {

                when(customerService.getAllCustomers())
                                .thenReturn(List.of(customerResponse()));

                mockMvc.perform(
                                get("/api/customers")
                                                .header(
                                                                "X-Correlation-Id",
                                                                "list-correlation-id"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath(
                                                "$.data").isArray())
                                .andExpect(jsonPath(
                                                "$.data.length()").value(1))
                                .andExpect(jsonPath(
                                                "$.data[0].customerNumber").value("CUST123456789"));
        }

        @Test
        void shouldUpdateCustomer() throws Exception {

                UpdateCustomerRequest request = new UpdateCustomerRequest(
                                "Updated",
                                "Jain",
                                "updated@example.com",
                                "+919876543211",
                                "Updated Address");

                when(customerService.updateCustomer(
                                eq(customerId),
                                any(UpdateCustomerRequest.class))).thenReturn(customerResponse());

                mockMvc.perform(
                                put("/api/customers/{id}", customerId)
                                                .header(
                                                                "X-Correlation-Id",
                                                                "update-correlation-id")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath(
                                                "$.message").value("Customer updated successfully"))
                                .andExpect(jsonPath(
                                                "$.correlationId").value("update-correlation-id"));
        }

        @Test
        void shouldRejectInvalidUpdateRequest() throws Exception {

                UpdateCustomerRequest request = new UpdateCustomerRequest(
                                "",
                                "",
                                "invalid-email",
                                "123",
                                "");

                mockMvc.perform(
                                put("/api/customers/{id}", customerId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath(
                                                "$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldRejectDuplicateEmail() throws Exception {

                CreateCustomerRequest request = new CreateCustomerRequest(
                                "Nikhar",
                                "Jain",
                                "nikhar@example.com",
                                "+919876543210",
                                LocalDate.of(1999, 1, 1),
                                "Indore");

                when(customerService.createCustomer(any(
                                CreateCustomerRequest.class))).thenThrow(
                                                new CustomerBusinessException(
                                                                "CUSTOMER_EMAIL_ALREADY_EXISTS",
                                                                "Customer with email already exists"));

                mockMvc.perform(
                                post("/api/customers")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath(
                                                "$.error").value("CUSTOMER_EMAIL_ALREADY_EXISTS"))
                                .andExpect(jsonPath(
                                                "$.message").value("Customer with email already exists"));
        }

        @Test
        void shouldDeactivateCustomer() throws Exception {

                CustomerResponse inactiveResponse = new CustomerResponse(
                                customerId,
                                "CUST123456789",
                                "Nikhar",
                                "Jain",
                                "nikhar@example.com",
                                "+919876543210",
                                LocalDate.of(1999, 1, 1),
                                "Indore",
                                CustomerStatus.INACTIVE,
                                OffsetDateTime.now(),
                                OffsetDateTime.now());

                when(customerService.deactivateCustomer(customerId))
                                .thenReturn(inactiveResponse);

                mockMvc.perform(
                                patch(
                                                "/api/customers/{id}/deactivate",
                                                customerId)
                                                .header(
                                                                "X-Correlation-Id",
                                                                "deactivate-correlation-id"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath(
                                                "$.message").value("Customer deactivated successfully"))
                                .andExpect(jsonPath(
                                                "$.data.status").value("INACTIVE"))
                                .andExpect(jsonPath(
                                                "$.correlationId").value("deactivate-correlation-id"));
        }

        @Test
        void shouldRejectAlreadyInactiveCustomer() throws Exception {

                when(customerService.deactivateCustomer(customerId))
                                .thenThrow(
                                                new CustomerBusinessException(
                                                                "CUSTOMER_ALREADY_INACTIVE",
                                                                "Customer is already inactive"));

                mockMvc.perform(
                                patch(
                                                "/api/customers/{id}/deactivate",
                                                customerId))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath(
                                                "$.error").value("CUSTOMER_ALREADY_INACTIVE"));
        }
}
