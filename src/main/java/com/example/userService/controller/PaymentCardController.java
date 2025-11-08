package com.example.userService.controller;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.service.PaymentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment_cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @PostMapping("/users/{userId}")
    public ResponseEntity<PaymentCardDTO> createCard(
            @RequestBody PaymentCardDTO paymentCardDTO,
            @PathVariable Long userId) {

        var card = paymentCardMapper.toEntity(paymentCardDTO);
        var createdCard = paymentCardService.createCard(card, userId);
        var createdCardDTO = paymentCardMapper.toDTO(createdCard);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCardDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getCardById(@PathVariable Long id) {
        var card = paymentCardService.getCardById(id);
        var cardDTO = paymentCardMapper.toDTO(card);
        return ResponseEntity.ok(cardDTO);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAllCards(
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(page, size);
        var cardsPage = paymentCardService.getAllCards(pageable);
        var cardsDTOPage = cardsPage.map(paymentCardMapper::toDTO);
        return ResponseEntity.ok(cardsDTOPage);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentCardDTO>> getCardsByUserId(@PathVariable Long userId) {
        var cards = paymentCardService.getCardsByUserId(userId);
        var cardsDTO = cards.stream()
                .map(paymentCardMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardsDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> updateCard(
            @PathVariable Long id,
            @RequestBody PaymentCardDTO paymentCardDTO) {

        var card = paymentCardMapper.toEntity(paymentCardDTO);
        var updatedCard = paymentCardService.updateCard(id, card);
        var updatedCardDTO = paymentCardMapper.toDTO(updatedCard);
        return ResponseEntity.ok(updatedCardDTO);
    }
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> activateOrDeactivateCard(
            @PathVariable Long id,
            @RequestParam Boolean active) {

        paymentCardService.activateOrDeactivateCard(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}