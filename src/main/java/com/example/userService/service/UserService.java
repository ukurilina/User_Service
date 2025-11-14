package com.example.userService.service;

import com.example.userService.dto.UserDTO;
import com.example.userService.entity.User;
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

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.hasFirstName(firstName))
                .and(UserSpecifications.hasSurname(surname));
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toDTO);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setBirthDate(userDTO.getBirthDate());
        user.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Transactional
    public void activateOrDeactivateUser(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.updateActiveStatus(id, active);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }
}