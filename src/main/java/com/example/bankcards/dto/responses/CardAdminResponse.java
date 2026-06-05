package com.example.bankcards.dto.responses;

import java.math.BigDecimal;

public record CardAdminResponse (
        String cardNumber,
        String owner,
        String id,
        String expirationDate,
        BigDecimal balance
){
}
