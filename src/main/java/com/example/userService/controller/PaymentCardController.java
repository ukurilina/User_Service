package com.example.userService.controller;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.service.PaymentCardService;
import com.example.userService.exception.PaymentCardNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/payment_cards")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping("/users/{userId}")
    public PaymentCardDTO createCard(
            @RequestBody PaymentCardDTO paymentCardDTO,
            @PathVariable Long userId) {

        return paymentCardService.createCard(paymentCardDTO, userId);
    }

    @GetMapping("/{id}")
    public PaymentCardDTO getCardById(@PathVariable Long id) {
        return paymentCardService.getCardById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
    }

    @GetMapping
    public Page<PaymentCardDTO> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return paymentCardService.getAllCards(pageable);
    }

    @GetMapping("/users/{userId}")
    public List<PaymentCardDTO> getCardsByUserId(@PathVariable Long userId) {
        return paymentCardService.getCardsByUserId(userId);
    }

    @PutMapping("/{id}")
    public PaymentCardDTO updateCard(
            @PathVariable Long id,
            @RequestBody PaymentCardDTO paymentCardDTO) {

        return paymentCardService.updateCard(id, paymentCardDTO);
    }

    @PatchMapping("/{id}/active")
    public void activateOrDeactivateCard(@PathVariable Long id, @RequestParam Boolean active) {
        paymentCardService.activateOrDeactivateCard(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}