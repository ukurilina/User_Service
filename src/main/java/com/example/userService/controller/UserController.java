package com.example.userService.controller;

import com.example.userService.dto.UserDTO;
import com.example.userService.mapper.UserMapper;
import com.example.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        var userEntity = userMapper.toEntity(userDTO);
        var createdUserEntity = userService.createUser(userEntity);
        return userMapper.toDTO(createdUserEntity);
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        var userEntity = userService.getUserById(id).orElse(null);
        return userMapper.toDTO(userEntity);
    }
}
