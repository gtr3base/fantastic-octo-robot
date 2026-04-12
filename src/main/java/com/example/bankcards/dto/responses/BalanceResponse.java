package com.example.bankcards.dto.responses;

public record BalanceResponse(
        Long cardId,
        Long balance
) {
}
