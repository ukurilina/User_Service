package com.example.userService.controller;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.service.PaymentCardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/payment_cards")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<PaymentCardDTO> createCard(
            @RequestBody PaymentCardDTO paymentCardDTO,
            @PathVariable Long userId) {

        PaymentCardDTO createdCard = paymentCardService.createCard(paymentCardDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard); // 201 Created
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getCardById(@PathVariable Long id) {
        PaymentCardDTO card = paymentCardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentCardDTO> cards = paymentCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentCardDTO>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCardDTO> cards = paymentCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> updateCard(
            @PathVariable Long id,
            @RequestBody PaymentCardDTO paymentCardDTO) {

        PaymentCardDTO updatedCard = paymentCardService.updateCard(id, paymentCardDTO);
        return ResponseEntity.ok(updatedCard);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<PaymentCardDTO> activateOrDeactivateCard(
            @PathVariable Long id,
            @RequestParam Boolean active) {

        PaymentCardDTO updatedCard = paymentCardService.activateOrDeactivateCard(id, active);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}