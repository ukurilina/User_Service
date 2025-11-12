package com.example.userService.controller;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.service.PaymentCardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment_cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardController(PaymentCardService paymentCardService, PaymentCardMapper paymentCardMapper) {
        this.paymentCardService = paymentCardService;
        this.paymentCardMapper = paymentCardMapper;
    }


    @PostMapping("/users/{userId}")
    public PaymentCardDTO createCard(
            @RequestBody PaymentCardDTO paymentCardDTO,
            @PathVariable Long userId) {

        var card = paymentCardMapper.toEntity(paymentCardDTO);
        var createdCard = paymentCardService.createCard(card, userId);
        return paymentCardMapper.toDTO(createdCard);
    }

    @GetMapping("/{id}")
    public PaymentCardDTO getCardById(@PathVariable Long id) {
        var card = paymentCardService.getCardById(id).orElse(null);
        return paymentCardMapper.toDTO(card);
    }

    @GetMapping
    public Page<PaymentCardDTO> getAllCards(@RequestParam int page, @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        var cardsPage = paymentCardService.getAllCards(pageable);
        return cardsPage.map(paymentCardMapper::toDTO);
    }

    @GetMapping("/users/{userId}")
    public List<PaymentCardDTO> getCardsByUserId(@PathVariable Long userId) {
        var cards = paymentCardService.getCardsByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public PaymentCardDTO updateCard(
            @PathVariable Long id,
            @RequestBody PaymentCardDTO paymentCardDTO) {

        var card = paymentCardMapper.toEntity(paymentCardDTO);
        var updatedCard = paymentCardService.updateCard(id, card);
        return paymentCardMapper.toDTO(updatedCard);
    }

    @PatchMapping("/{id}/active")
    public void activateOrDeactivateCard(@PathVariable Long id, @RequestParam Boolean active) {
        paymentCardService.activateOrDeactivateCard(id, active);
    }

    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
    }
}