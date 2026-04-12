package com.example.bankcards.dto.responses;

import com.example.bankcards.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email,
        UserRole role,
        LocalDateTime createdAt
) { }
