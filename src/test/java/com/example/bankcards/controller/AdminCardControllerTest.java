package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CardRequest;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminCardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminCardController adminCardController;

    @Test
    @DisplayName("Create Card - Success")
    void createCard_Success() {
        CardRequest request = mock(CardRequest.class);
        CardResponse mockResponse = mock(CardResponse.class);

        when(cardService.createCard(request)).thenReturn(mockResponse);

        ResponseEntity<CardResponse> response = adminCardController.createCard(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(cardService, times(1)).createCard(request);
    }

    @Test
    @DisplayName("Get All Cards - Success")
    void getAllCards_Success() {
        CardResponse card1 = mock(CardResponse.class);
        CardResponse card2 = mock(CardResponse.class);
        when(cardService.getAllCards()).thenReturn(List.of(card1, card2));

        ResponseEntity<List<CardResponse>> response = adminCardController.getAllCards();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2).containsExactly(card1, card2);
        verify(cardService, times(1)).getAllCards();
    }

    @Test
    @DisplayName("Activate Card - Success")
    void activateCard_Success() {
        CardResponse mockResponse = mock(CardResponse.class);
        when(cardService.activateCard(1L)).thenReturn(mockResponse);

        ResponseEntity<CardResponse> response = adminCardController.activateCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(cardService, times(1)).activateCard(1L);
    }

    @Test
    @DisplayName("Delete Card - Success")
    void deleteCard_Success() {
        doNothing().when(cardService).deleteCard(1L);

        ResponseEntity<Void> response = adminCardController.deleteCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(cardService, times(1)).deleteCard(1L);
    }
}