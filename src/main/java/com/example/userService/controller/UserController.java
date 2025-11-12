package com.example.userService.controller;

import com.example.userService.dto.UserDTO;
import com.example.userService.mapper.UserMapper;
import com.example.userService.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        var user = userMapper.toEntity(userDTO);
        var createdUser = userService.createUser(user);
        return userMapper.toDTO(createdUser);
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        var user = userService.getUserById(id).orElse(null);
        return userMapper.toDTO(user);
    }

    @GetMapping
    public Page<UserDTO> getAllUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(page, size);
        var usersPage = userService.getAllUsers(firstName, surname, pageable);
        return usersPage.map(userMapper::toDTO);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        var user = userMapper.toEntity(userDTO);
        var updatedUser = userService.updateUser(id, user);
        return userMapper.toDTO(updatedUser);
    }

    @PatchMapping("/{id}/active")
    public void activateOrDeactivateUser(@PathVariable Long id, @RequestParam Boolean active) {
        userService.activateOrDeactivateUser(id, active);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}