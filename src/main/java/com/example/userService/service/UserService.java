package com.example.userService.service;

import com.example.userService.entity.*;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import com.example.userService.specification.UserSpecifications;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;

    public UserService(UserRepository userRepository, PaymentCardRepository paymentCardRepository) {
        this.userRepository = userRepository;
        this.paymentCardRepository = paymentCardRepository;
    }

    @CacheEvict(value = {"userCache", "userWithCardsCache"}, allEntries = true)
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Cacheable(value = "userCache", key = "#id")
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "userWithCardsCache", key = "#userId")
    @Transactional(readOnly = true)
    public Map<String, Object> getUserWithCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("cards", cards);
        result.put("cardsCount", cards.size());

        return result;
    }

    @Cacheable(value = "userCache")
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.hasFirstName(firstName)).and(UserSpecifications.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @Caching(evict = {
            @CacheEvict(value = "cardCache", key = "#id"),
            @CacheEvict(value = "userCardsCache", key = "#cardDetails.user.id"),
            @CacheEvict(value = "userWithCardsCache", key = "#cardDetails.user.id")
    })
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setSurname(userDetails.getSurname());
                    user.setBirthDate((userDetails.getBirthDate()));
                    user.setEmail(userDetails.getEmail());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "#id"),
            @CacheEvict(value = "userWithCardsCache", key = "#id")
    })
    @Transactional
    public void activateOrDeactivateUser(Long id, Boolean active) {
        userRepository.updateActiveStatus(id, active);
    }

    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "#id"),
            @CacheEvict(value = "userWithCardsCache", key = "#id")
    })
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
