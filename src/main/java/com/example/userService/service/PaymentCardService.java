package com.example.userService.service;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardService(PaymentCardRepository paymentCardRepository,
                              UserRepository userRepository,
                              PaymentCardMapper paymentCardMapper) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Transactional
    public PaymentCardDTO createCard(PaymentCardDTO cardDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        int activeCardCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardCount >= 5) {
            throw new RuntimeException("User can't have more than 5 cards");
        }

        PaymentCard card = paymentCardMapper.toEntity(cardDTO);
        card.setUser(user);
        PaymentCard savedCard = paymentCardRepository.save(card);
        return paymentCardMapper.toDTO(savedCard);
    }

    @Transactional(readOnly = true)
    public PaymentCardDTO getCardById(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        return paymentCardMapper.toDTO(card);
    }

    @Transactional(readOnly = true)
    public Page<PaymentCardDTO> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable)
                .map(paymentCardMapper::toDTO);
    }

    @Transactional
    public List<PaymentCardDTO> getCardsByUserId(Long userId) {
        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDTO)
                .toList();
    }

    @Transactional
    public PaymentCardDTO updateCard(Long id, PaymentCardDTO cardDTO) {
        return paymentCardRepository.findById(id)
                .map(card -> {
                    card.setNumber(cardDTO.getNumber());
                    card.setHolder(cardDTO.getHolder());
                    card.setExpirationDate(cardDTO.getExpirationDate());
                    PaymentCard updatedCard = paymentCardRepository.save(card);
                    return paymentCardMapper.toDTO(updatedCard);
                })
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
    }

    @Transactional
    public void activateOrDeactivateCard(Long id, Boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        paymentCardRepository.updateActiveStatus(id, active);
    }

    @Transactional
    public void deleteCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        paymentCardRepository.delete(card);
    }
}