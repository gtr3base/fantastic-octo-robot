package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CardTransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserCardController userCardController;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(userService.getCurrentUserId(principal)).thenReturn(userId);
    }

    @Test
    @DisplayName("Get My Cards - Success")
    void getMyCards_Success() {
        @SuppressWarnings("unchecked")
        Page<CardResponse> mockPage = mock(Page.class);

        when(cardService.getUserCards(eq(userId), eq("active"), any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<CardResponse>> response =
                userCardController.getMyCards(principal, "active", 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockPage);
        verify(cardService, times(1)).getUserCards(eq(userId), eq("active"), any(PageRequest.class));
    }

    @Test
    @DisplayName("Block Card - Success")
    void blockCard_Success() {
        CardResponse mockResponse = mock(CardResponse.class);
        when(cardService.blockCard(userId, 100L)).thenReturn(mockResponse);

        ResponseEntity<CardResponse> response = userCardController.blockCard(principal, 100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(cardService, times(1)).blockCard(userId, 100L);
    }

    @Test
    @DisplayName("Transfer Between Own Cards - Success")
    void transfer_Success() {
        CardTransferRequest request = new CardTransferRequest(100L, 200L, 5000L);
        doNothing().when(cardService).transferBetweenOwnCards(userId, request);

        ResponseEntity<String> response = userCardController.transfer(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Transfer completed successfully");
        verify(cardService, times(1)).transferBetweenOwnCards(userId, request);
    }

    @Test
    @DisplayName("Get Balance - Success")
    void getBalance_Success() {
        BalanceResponse mockBalance = mock(BalanceResponse.class);
        when(cardService.getCardBalance(userId, 100L)).thenReturn(mockBalance);

        ResponseEntity<BalanceResponse> response = userCardController.getBalance(principal, 100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockBalance);
        verify(cardService, times(1)).getCardBalance(userId, 100L);
    }
}