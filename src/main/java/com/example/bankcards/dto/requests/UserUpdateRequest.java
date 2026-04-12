package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String name,
        String surname,

        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(min = 6, max = 255, message = "Password minimum length is 6 and max 255")
        String password
) {
}
