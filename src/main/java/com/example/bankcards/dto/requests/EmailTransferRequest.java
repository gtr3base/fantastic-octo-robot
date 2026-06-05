package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EmailTransferRequest(
        @NotNull Long fromCardId,
        @NotNull String ownerEmail,
        @NotNull @Min(value = 1, message = "Transfer amount must be at least 1 cent") Long amount
) implements TransferRequest {}
