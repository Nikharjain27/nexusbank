package com.nexusbank.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^\\+?[1-9]\\d{9,14}$",
                message = "Phone number must be valid"
        )
        String phone,

        @NotBlank(message = "Address is required")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
}