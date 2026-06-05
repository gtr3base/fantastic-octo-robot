package com.example.bankcards.dto.requests;

public sealed interface TransferRequest permits CardTransferRequest, EmailTransferRequest{
    Long fromCardId();
    Long amount();
}
