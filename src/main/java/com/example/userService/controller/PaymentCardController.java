package com.example.userService.controller;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.service.PaymentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment_cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @PostMapping
    public PaymentCardDTO createCard(@RequestBody PaymentCardDTO paymentCardDTO) {
        var cardEntity = paymentCardMapper.toEntity(paymentCardDTO);
        var createdCardEntity = paymentCardService.createCard(cardEntity, paymentCardDTO.getUserId());
        return paymentCardMapper.toDTO(createdCardEntity);
    }

    @GetMapping("/{id}")
    public PaymentCardDTO getCardById(@PathVariable Long id) {
        var cardEntity = paymentCardService.getCardById(id).orElse(null);
        return paymentCardMapper.toDTO(cardEntity);
    }
}
