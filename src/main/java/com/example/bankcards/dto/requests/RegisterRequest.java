package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
    @NotBlank @Size(min = 1, max = 50) String name,
    @NotBlank @Size(min = 1, max = 50) String surname,
    @NotBlank @Email @Size(max = 100)  String email,
    @NotBlank @Size(min = 6, max = 255) String password
){}