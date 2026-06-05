package com.example.bankcards.mappers;

import com.example.bankcards.dto.requests.CardRequest;
import com.example.bankcards.dto.responses.CardAdminResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", imports = {YearMonth.class, DateTimeFormatter.class})
public abstract class CardMapper {

    private static final String EMAIL_NOT_FOUND = "Email %s not found";
    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expirationDate", expression = "java(parseExpirationDate(cardRequest.expirationDate()))")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "owner", expression = "java(getOwner(cardRequest.ownerEmail()))")
    public abstract Card toCard(CardRequest cardRequest);

    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "owner", source = "owner.email")
    @Mapping(target = "expirationDate", expression = "java(parseExpirationDate(card.getExpirationDate()))")
    public abstract CardResponse toCardResponse(Card card);

    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "owner", source = "owner.email")
    @Mapping(target = "expirationDate", expression = "java(parseExpirationDate(card.getExpirationDate()))")
    public abstract CardAdminResponse toCardAdminResponse(Card card);

    protected YearMonth parseExpirationDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        return YearMonth.parse(date, formatter);
    }

    protected String parseExpirationDate(YearMonth date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        return date.format(formatter);
    }

    protected User getOwner(String ownerEmail) {
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new EmailNotFoundException(String.format(EMAIL_NOT_FOUND, ownerEmail)));
    }
}
