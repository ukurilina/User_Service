package com.example.userService.mapper;

import com.example.userService.dto.UserDTO;
import com.example.userService.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}
