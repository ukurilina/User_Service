package com.example.userService.service;

import com.example.userService.dto.UserDTO;
import com.example.userService.entity.User;
import com.example.userService.exception.UserNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import com.example.userService.mapper.UserMapper;
import com.example.userService.repository.UserRepository;
import com.example.userService.specification.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @CacheEvict(value = {"userCache", "userWithCardsCache"}, allEntries = true)
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Cacheable(value = "userCache", key = "#id")
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDTO(user);
    }

    @Cacheable(value = "userCache")
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.hasFirstName(firstName))
                .and(UserSpecifications.hasSurname(surname));
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toDTO);
    }

    @Caching(evict = {
            @CacheEvict(value = "cardCache", key = "#id"),
            @CacheEvict(value = "userCardsCache", key = "#cardDetails.user.id"),
            @CacheEvict(value = "userWithCardsCache", key = "#cardDetails.user.id")
    })
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setBirthDate(userDTO.getBirthDate());
        user.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "#id"),
            @CacheEvict(value = "userWithCardsCache", key = "#id")
    })
    @Transactional
    public void activateOrDeactivateUser(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.updateActiveStatus(id, active);
    }

    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "#id"),
            @CacheEvict(value = "userWithCardsCache", key = "#id")
    })
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }
}