package com.example.userService.controller;

import com.example.userService.dto.UserDTO;
import com.example.userService.mapper.UserMapper;
import com.example.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        var user = userMapper.toEntity(userDTO);
        var createdUser = userService.createUser(user);
        var createdUserDTO = userMapper.toDTO(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        var user = userService.getUserById(id);
        var userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(page, size);
        var usersPage = userService.getAllUsers(firstName, surname, pageable);
        var usersDTOPage = usersPage.map(userMapper::toDTO);
        return ResponseEntity.ok(usersDTOPage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        var user = userMapper.toEntity(userDTO);
        var updatedUser = userService.updateUser(id, user);
        var updatedUserDTO = userMapper.toDTO(updatedUser);
        return ResponseEntity.ok(updatedUserDTO);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> activateOrDeactivateUser(@PathVariable Long id, @RequestParam Boolean active) {
        userService.activateOrDeactivateUser(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}