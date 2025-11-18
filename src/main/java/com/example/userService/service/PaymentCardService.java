package com.example.userService.service;

import com.example.userService.dto.PaymentCardDto;
import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Transactional
    public PaymentCardDto createCard(PaymentCardDto cardDTO, Long userId) {
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
    public PaymentCardDto getCardById(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        return paymentCardMapper.toDTO(card);
    }

    @Transactional(readOnly = true)
    public Page<PaymentCardDto> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable)
                .map(paymentCardMapper::toDTO);
    }

    @Transactional
    public List<PaymentCardDto> getCardsByUserId(Long userId) {
        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDTO)
                .toList();
    }

    @Transactional
    public PaymentCardDto updateCard(Long id, PaymentCardDto cardDTO) {
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
    public PaymentCardDto activateOrDeactivateCard(Long id, Boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        paymentCardRepository.updateActiveStatus(id, active);

        card.setActive(active);
        return paymentCardMapper.toDTO(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        paymentCardRepository.delete(card);
    }
}