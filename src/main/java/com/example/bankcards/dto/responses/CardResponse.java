package com.example.bankcards.dto.responses;

import com.example.bankcards.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CardResponse(
        @JsonIgnore String cardNumber,
        String owner,
        String id,
        String expirationDate,
        CardStatus status,
        BigDecimal balance
) {
    private static final String HIDDEN = "**** **** **** ";

    @JsonProperty("cardNumber")
    public String getMaskedCardNumber() {
        if(cardNumber!=null){
            return HIDDEN + this.cardNumber.substring(this.cardNumber.length() - 4);
        }
        return null;
    }

}
