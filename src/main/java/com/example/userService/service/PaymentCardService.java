package com.example.userService.service;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.exception.CardLimitExceededException;
import com.example.userService.exception.PaymentCardNotFoundException;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @CacheEvict(value = {"userCardsCache"}, key = "#userId")
    @Transactional
    public PaymentCardDTO createCard(PaymentCardDTO cardDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        int activeCardCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardCount >= 5) {
            throw new CardLimitExceededException(userId);
        }

        PaymentCard card = paymentCardMapper.toEntity(cardDTO);
        card.setUser(user);
        PaymentCard savedCard = paymentCardRepository.save(card);
        return paymentCardMapper.toDTO(savedCard);
    }

    @Cacheable(value = "cards", key = "#id")
    @Transactional(readOnly = true)
    public PaymentCardDTO getCardById(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        return paymentCardMapper.toDTO(card);
    }

    @Cacheable(value = "cards")
    @Transactional(readOnly = true)
    public Page<PaymentCardDTO> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAllCards(pageable)
                .map(paymentCardMapper::toDTO);
    }

    @CachePut(value = "cards", key = "#userId")
    @Transactional
    public List<PaymentCardDTO> getCardsByUserId(Long userId) {
        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDTO)
                .toList();
    }

    @CachePut(value = "cards", key = "#id")
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
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
    }

    @CachePut(value = "cards", key = "#id")
    @Transactional
    public void activateOrDeactivateCard(Long id, Boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.updateActiveStatus(id, active);
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "userCardsCache", key = "#userId")
    })
    @Transactional
    public void deleteCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.delete(card);
    }
}