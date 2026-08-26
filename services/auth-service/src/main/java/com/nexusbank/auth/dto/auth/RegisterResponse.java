package com.nexusbank.auth.dto.auth;

import java.util.UUID;

public class RegisterResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(
            UUID userId,
            String email,
            String firstName,
            String lastName,
            String message
    ) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMessage() {
        return message;
    }
}