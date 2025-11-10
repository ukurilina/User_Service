package com.example.userService.service;

import com.example.userService.entity.User;
import com.example.userService.repository.UserRepository;
import com.example.userService.specification.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Page<User> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.hasFirstName(firstName)).and(UserSpecifications.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }
    @Transactional
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setSurname(userDetails.getSurname());
                    user.setBirthDate((userDetails.getBirthDate()));
                    user.setEmail(userDetails.getEmail());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User is not found"));
    }
    @Transactional
    public void activateOrDeactivateUser(Long id, Boolean active) {
        userRepository.updateActiveStatus(id, active);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
