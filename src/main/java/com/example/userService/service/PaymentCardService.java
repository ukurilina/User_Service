package com.example.userService.service;

import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import com.example.userService.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Service
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    public PaymentCardService(PaymentCardRepository paymentCardRepository, UserRepository userRepository) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = {"userCardsCache"}, key = "#userId")
    public PaymentCard createCard(PaymentCard card, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentCardNotFoundException(userId));

        int activeCardCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardCount >= 5) {
            throw new CardLimitExceededException(userId);
        }

        card.setUser(user);
        return paymentCardRepository.save(card);
    }

    @Cacheable(value = "cards", key = "#id")
    @Transactional(readOnly = true)
    public Optional<PaymentCard> getCardById(Long id) {
        return paymentCardRepository.findById(id);
    }

    @Cacheable(value = "cards")
    @Transactional(readOnly = true)
    public Page<PaymentCard> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable);
    }

    @Cacheable(value = "userCardsCache", key = "#userId")
    @Transactional(readOnly = true)
    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId);
    }

    @CachePut(value = "cards", key = "#id")
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

    @CachePut(value = "cards", key = "#id")
    @Transactional
    public void activateOrDeactivateCard(Long id, Boolean active) {
        paymentCardRepository.updateActiveStatus(id, active);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "userCardsCache", key = "#userId")
    })
    public void deleteCard(Long id) {
        Long userId = paymentCardRepository.findById(id)
                .map(card -> card.getUser().getId())
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.deleteById(id);
    }
}
