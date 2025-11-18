package com.example.userService.mapper;

import com.example.userService.dto.UserDto;
import com.example.userService.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDTO(User user);
    User toEntity(UserDto userDTO);
}
