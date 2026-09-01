package com.nexusbank.customer.dto.response;

import com.nexusbank.customer.domain.enums.CustomerStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(

        UUID id,

        String customerNumber,

        String firstName,

        String lastName,

        String email,

        String phone,

        LocalDate dateOfBirth,

        String address,

        CustomerStatus status,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt) {
}