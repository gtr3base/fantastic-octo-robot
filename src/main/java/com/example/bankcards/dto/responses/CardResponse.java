package com.example.bankcards.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CardResponse(
        @JsonIgnore String cardNumber,
        String owner,
        String expirationDate,
        @JsonIgnore Long balance
) {
    private static final String HIDDEN = "**** **** **** ";

    @JsonProperty("cardNumber")
    public String getMaskedCardNumber() {
        if(cardNumber!=null){
            return HIDDEN + this.cardNumber.substring(this.cardNumber.length() - 4);
        }
        return null;
    }

    @JsonProperty("balance")
    public Long getBalance(){
        if(balance!=null){
            return balance/100;
        }
        return null;
    }
}
