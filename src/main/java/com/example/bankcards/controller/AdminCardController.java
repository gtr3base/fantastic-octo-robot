package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CardRequest;
import com.example.bankcards.dto.responses.CardAdminResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cards")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardAdminResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        CardAdminResponse response = cardService.createCard(cardRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CardAdminResponse>> getAllCards() {
        List<CardAdminResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long cardId) {
        CardResponse response = cardService.activateCard(cardId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
