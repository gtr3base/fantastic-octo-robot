package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CardRequest(
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{4} \\d{4} \\d{4} \\d{4}", message = "Card number must be in format: xxxx xxxx xxxx xxxx")
    String cardNumber,

    @NotBlank(message = "Owner email is required") String ownerEmail,

    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiration date must be MM/YY")
    String expirationDate,

    @NotNull(message = "balance is required")
    @PositiveOrZero(message = "Balance must be positive or zero")
    Long balance
) {
    public Long getBalance() {
        return balance*100;
    }
}
