package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CardTransferRequest;
import com.example.bankcards.dto.requests.EmailTransferRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.responses.BalanceResponse;
import com.example.bankcards.dto.responses.CardResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user/cards")
public class UserCardController {

    private final CardService cardService;
    private final UserService userService;

    public UserCardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
                Long userId = userService.getCurrentUserId(principal);
                Page<CardResponse> cards = cardService.getUserCards(userId, search, PageRequest.of(page, size));
                return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardResponse> blockCard(Principal principal,@PathVariable Long cardId) {
        Long userId = userService.getCurrentUserId(principal);

        CardResponse response = cardService.blockCard(userId,cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(Principal principal, @Valid @RequestBody CardTransferRequest request) {
        Long userId = userService.getCurrentUserId(principal);

        cardService.transferBetweenOwnCards(userId, request);
        return ResponseEntity.ok("Transfer completed successfully");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(Principal principal, @PathVariable Long cardId) {
        Long userId = userService.getCurrentUserId(principal);

        BalanceResponse balance = cardService.getCardBalance(userId, cardId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/transfer/external")
    public ResponseEntity<Void> transferToOtherUser(Principal principal, @RequestBody EmailTransferRequest request) {
        Long userId = userService.getCurrentUserId(principal);
        cardService.transferToAnotherUserCard(userId, request);
        return ResponseEntity.ok().build();
    }
}
