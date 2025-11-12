package com.example.userService.service;

import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.exception.PaymentCardNotFoundException;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    public PaymentCardService(PaymentCardRepository paymentCardRepository,
                              UserRepository userRepository) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }
    public PaymentCard createCard(PaymentCard card, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User is not found"));

        int activeCardCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardCount >= 5) {
            throw new RuntimeException("User cannot have more than 5 active cards");
        }

        card.setUser(user);
        return paymentCardRepository.save(card);
    }

    public Optional<PaymentCard> getCardById(Long id) {
        return paymentCardRepository.findById(id);
    }

    public Page<PaymentCard> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable);
    }

    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId);
    }

    public PaymentCard updateCard(Long id, PaymentCard cardDetails) {
        return paymentCardRepository.findById(id)
                .map(card -> {
                    card.setNumber(cardDetails.getNumber());
                    card.setHolder(cardDetails.getHolder());
                    card.setExpirationDate(cardDetails.getExpirationDate());
                    return paymentCardRepository.save(card);
                })
                .orElseThrow(() -> new RuntimeException("Card is not found"));
    }
    public void activateOrDeactivateCard(Long id, Boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.updateActiveStatus(id, active);
    }
    public void deleteCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.delete(card);
    }
}
