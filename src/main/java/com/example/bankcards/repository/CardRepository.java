package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card,Long> {
    Optional<Card> findCardById(Long id);

    Optional<Card> findCardByIdAndOwnerId(Long cardId, Long userId);

    Page<Card> findByOwnerId(Long userId, Pageable pageable);

    Optional<Card> findFirstByOwnerEmail(String ownerEmail);

    Page<Card> findByOwnerIdAndCardNumberContaining(Long userId, String cardNumber, Pageable pageable);
}
