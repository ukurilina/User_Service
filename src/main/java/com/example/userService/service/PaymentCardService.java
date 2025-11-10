package com.example.userService.service;

import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.exception.CardLimitExceededException;
import com.example.userService.exception.PaymentCardNotFoundException;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCardService {

    private PaymentCardRepository paymentCardRepository;
    private UserRepository userRepository;

    public PaymentCard createCard(PaymentCard card, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        int activeCardCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardCount >= 5) {
            throw new CardLimitExceededException(userId);
        }

        card.setUser(user);
        return paymentCardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public PaymentCard getCardById(Long id) {

        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
    }

    public Page<PaymentCard> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable);
    }

    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId);
    }

    @Transactional
    public PaymentCard updateCard(Long id, PaymentCard cardDetails) {
        return paymentCardRepository.findById(id)
                .map(card -> {
                    card.setNumber(cardDetails.getNumber());
                    card.setHolder(cardDetails.getHolder());
                    card.setExpirationDate(cardDetails.getExpirationDate());
                    return paymentCardRepository.save(card);
                })
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
    }

    @Transactional
    public void activateOrDeactivateCard(Long id, Boolean active) {
        paymentCardRepository.updateActiveStatus(id, active);
    }

    @Transactional
    public void deleteCard(Long id) {
        paymentCardRepository.deleteById(id);
    }
}
