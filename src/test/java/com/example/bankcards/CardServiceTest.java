package com.example.bankcards;

import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.mappers.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").build();

        fromCard = Card.builder()
                .id(100L)
                .owner(testUser)
                .balance(5000L)
                .status(CardStatus.ACTIVE)
                .expirationDate(YearMonth.now().plusYears(1))
                .build();

        toCard = Card.builder()
                .id(200L)
                .owner(testUser)
                .balance(1000L)
                .status(CardStatus.ACTIVE)
                .expirationDate(YearMonth.now().plusYears(1))
                .build();
    }

    @Test
    @DisplayName("Transfer - Success")
    void transferBetweenOwnCards_Success() {
        TransferRequest request = new TransferRequest(100L, 200L, 2000L);

        when(cardRepository.findCardByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findCardByIdAndOwnerId(200L, 1L)).thenReturn(Optional.of(toCard));

        cardService.transferBetweenOwnCards(1L, request);

        assertThat(fromCard.getBalance()).isEqualTo(3000L);
        assertThat(toCard.getBalance()).isEqualTo(3000L);

        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
    }

    @Test
    @DisplayName("Transfer - Fails when same card")
    void transferBetweenOwnCards_SameCard() {
        TransferRequest request = new TransferRequest(100L, 100L, 2000L);

        assertThatThrownBy(() -> cardService.transferBetweenOwnCards(1L, request))
                .isInstanceOf(CardOperationException.class)
                .hasMessageContaining("Cannot transfer to");
    }

    @Test
    @DisplayName("Transfer - Fails when Insufficient Funds")
    void transferBetweenOwnCards_InsufficientFunds() {
        TransferRequest request = new TransferRequest(100L, 200L, 9000L);

        when(cardRepository.findCardByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findCardByIdAndOwnerId(200L, 1L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferBetweenOwnCards(1L, request))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    @DisplayName("Transfer - Fails when Sender Card is Not Active")
    void transferBetweenOwnCards_NotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferRequest request = new TransferRequest(100L, 200L, 1000L);

        when(cardRepository.findCardByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findCardByIdAndOwnerId(200L, 1L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferBetweenOwnCards(1L, request))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Card is not active");
    }

    @Test
    @DisplayName("Block Card - Success")
    void blockCard_Success() {
        when(cardRepository.findCardByIdAndOwnerId(1L, 100L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.save(any(Card.class))).thenReturn(fromCard);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(new CardResponse(null, null, null, null));

        cardService.blockCard(1L, 100L);

        assertThat(fromCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(fromCard);
    }

    @Test
    @DisplayName("Block Card - Fails when Already Blocked")
    void blockCard_AlreadyBlocked() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findCardByIdAndOwnerId(1L, 100L)).thenReturn(Optional.of(fromCard));

        assertThatThrownBy(() -> cardService.blockCard(1L, 100L))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Card is already blocked");
    }

    @Test
    @DisplayName("Delete Card - Fails when balance is > 0")
    void deleteCard_WithBalance() {
        when(cardRepository.findCardById(100L)).thenReturn(Optional.of(fromCard)); // Balance is 5000L

        assertThatThrownBy(() -> cardService.deleteCard(100L))
                .isInstanceOf(CardOperationException.class)
                .hasMessageContaining("Cannot delete card with positive balance");
    }

    @Test
    @DisplayName("Delete Card - Success when balance is 0")
    void deleteCard_Success() {
        fromCard.setBalance(0L);
        when(cardRepository.findCardById(100L)).thenReturn(Optional.of(fromCard));

        cardService.deleteCard(100L);

        verify(cardRepository, times(1)).delete(fromCard);
    }
}
