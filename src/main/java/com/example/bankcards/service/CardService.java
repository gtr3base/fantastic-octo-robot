package com.example.bankcards.service;

import com.example.bankcards.dto.requests.CardRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.repository.CardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardService {

    private static final String CARD_NOT_FOUND = "Card with id %s not found";
    private static final String CARD_ALREADY_BLOCKED = "Card is already blocked";
    private static final String CARD_EXPIRED = "Card expired";
    private static final String CARD_ALREADY_ACTIVE = "Card is already active";
    private static final String CARD_DELETION_WITH_BALANCE = "Cannot delete card with positive balance. Please withdraw funds first";
    private static final String CARD_TRANSFER_TO_FROM = "Cannot transfer to %s from %s";
    private static final String CARD_IS_NOT_ACTIVE = "Card is not active";
    private static final String INSUFFICIENT_FUNDS = "Insufficient funds";

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    public CardResponse createCard(CardRequest cardRequest){
        log.info("Creating new card for owner: {}", cardRequest.ownerEmail());

        Card card = cardMapper.toCard(cardRequest);

        Card savedCard = cardRepository.save(card);

        log.info("Card created successfully with ID: {}", card.getId());

        return cardMapper.toCardResponse(savedCard);
    }

    @Transactional
    public CardResponse blockCard(Long userId, Long cardId){
        log.info("Blocking card with ID: {}", cardId);

        Card card = cardRepository.findCardByIdAndOwnerId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException(String.format(CARD_NOT_FOUND, cardId)));

        if(card.getStatus() == CardStatus.BLOCKED){
            log.warn("Card with ID: {} is already blocked", cardId);
            throw new CardOperationException(CARD_ALREADY_BLOCKED);
        }

        if(card.isExpired()){
            log.warn("Card with ID: {} is expired", cardId);
            throw new CardOperationException(CARD_EXPIRED);
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);

        log.info("Card blocked successfully with ID: {}", cardId);

        return cardMapper.toCardResponse(updatedCard);
    }

    @Transactional
    public CardResponse activateCard(Long cardId){
        log.info("Activating card with ID: {}", cardId);

        Card card = cardRepository.findCardById(cardId)
                .orElseThrow(() -> new CardNotFoundException(String.format(CARD_NOT_FOUND, cardId)));

        if(card.getStatus() == CardStatus.ACTIVE){
            log.warn("Card with ID: {} is already active", cardId);
            throw new CardOperationException(CARD_ALREADY_ACTIVE);
        }

        if(card.isExpired()){
            log.warn("Cannot activate expired card with ID: {} is expired", cardId);
            throw new CardOperationException(CARD_EXPIRED);
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);

        log.info("Card activated successfully with ID: {}", cardId);

        return cardMapper.toCardResponse(updatedCard);
    }

    @Transactional
    public void deleteCard(Long cardId){
        log.info("Deleting card with ID: {}", cardId);

        Card card = cardRepository.findCardById(cardId)
                .orElseThrow(() -> new CardNotFoundException(String.format(CARD_NOT_FOUND, cardId)));

        if(card.getBalance() > 0){
            log.warn("Cannot delete card with ID: {} because it has non-zero balance: {}", cardId, card.getBalance());
            throw new CardOperationException(CARD_DELETION_WITH_BALANCE);
        }

        cardRepository.delete(card);

        log.info("Card deleted successfully with ID: {}", cardId);
    }

    @Transactional
    public void transferBetweenOwnCards(Long userId, TransferRequest req){
        log.info("Transfer between own cards for owner: {}", userId);
        if(req.fromCardId().equals(req.toCardId())){
            log.warn("Cannot transfer between same cards for owner: {}", userId);
            throw new CardOperationException(String.format(CARD_TRANSFER_TO_FROM, req.fromCardId(), req.toCardId()));
        }

        Card fromC = getCardIfBelongsToUser(req.fromCardId(), userId);
        Card toC = getCardIfBelongsToUser(req.toCardId(), userId);

        if(!CardStatus.ACTIVE.equals(fromC.getStatus())){
            log.warn("Cannot transfer when card: {} is inactive: {}",fromC.getId(), fromC.getStatus());
            throw new CardOperationException(CARD_IS_NOT_ACTIVE);
        }

        if(fromC.getBalance() < req.amount()){
            log.warn("Cannot transfer, insufficient funds");
            throw new CardOperationException(INSUFFICIENT_FUNDS);
        }

        fromC.setBalance(fromC.getBalance() - req.amount());
        toC.setBalance(toC.getBalance() + req.amount());

        cardRepository.save(fromC);
        cardRepository.save(toC);

        log.info("Amount transferred successfully with ID: {}", fromC.getId());
    }

    public BalanceResponse getCardBalance(Long userId, Long cardId) {
        log.info("Getting card balance for owner: {}", userId);
        Card card = getCardIfBelongsToUser(cardId, userId);
        return new BalanceResponse(card.getId(), card.getBalance());
    }

    public List<CardResponse> getAllCards() {
        log.info("Getting all cards");
        List<Card> cards = cardRepository.findAll();

        return cards.stream()
                .map(cardMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    public Page<CardResponse> getUserCards(Long userId, String search, Pageable pageable) {
        log.info("Getting user cards for owner: {}", userId);
        Page<Card> cards;
        if(search != null && !search.isBlank()){
            cards = cardRepository.findByOwnerIdAndCardNumberContaining(userId, search, pageable);
        }else {
            cards = cardRepository.findByOwnerId(userId, pageable);
        }
        return cards.map(cardMapper::toCardResponse);
    }

    private Card getCardIfBelongsToUser(Long cardId, Long userId) {
        log.info("Getting card with ID: {}", cardId);
        return cardRepository.findCardByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardOperationException(String.format(CARD_NOT_FOUND, cardId)));
    }
}
