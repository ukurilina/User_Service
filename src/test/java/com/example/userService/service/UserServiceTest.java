package com.example.userService.service;

import com.example.userService.dto.UserDTO;
import com.example.userService.entity.User;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.mapper.UserMapper;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldReturnSavedUserWithCorrectMapping() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ulyana");
        userDTO.setSurname("Kurylina");
        userDTO.setEmail("ulyana@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        User userEntity = new User();
        userEntity.setName("Ulyana");
        userEntity.setSurname("Kurylina");
        userEntity.setEmail("ulyana@example.com");
        userEntity.setBirthDate(LocalDate.of(1990, 1, 1));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Ulyana");
        savedUser.setSurname("Kurylina");
        savedUser.setEmail("ulyana@example.com");
        savedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        savedUser.setActive(true);

        UserDTO savedUserDTO = new UserDTO();
        savedUserDTO.setId(1L);
        savedUserDTO.setName("Ulyana");
        savedUserDTO.setSurname("Kurylina");
        savedUserDTO.setEmail("ulyana@example.com");
        savedUserDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        savedUserDTO.setActive(true);

        when(userMapper.toEntity(userDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(savedUserDTO);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ulyana", result.getName());
        assertEquals("Kurylina", result.getSurname());
        assertEquals("ulyana@example.com", result.getEmail());
        assertTrue(result.getActive());

        verify(userMapper, times(1)).toEntity(userDTO);
        verify(userRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).toDTO(savedUser);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserWithCorrectMapping() {
        Long userId = 1L;
        User userEntity = new User();
        userEntity.setId(userId);
        userEntity.setName("Ulyana");
        userEntity.setEmail("ulyana@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setName("Ulyana");
        userDTO.setEmail("ulyana@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDTO(userEntity)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Ulyana", result.getName());
        assertEquals("ulyana@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toDTO(userEntity);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    // GET ALL USERS
    @Test
    void getAllUsers_ShouldReturnPageWithCorrectMapping() {
        String firstName = "Ulyana";
        String surname = "Kurylina";
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(1L);
        user1.setName("Ulyana");
        user1.setSurname("Kurylina");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Ulyana");
        user2.setSurname("Kurylina");

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setName("Ulyana");
        userDTO1.setSurname("Kurylina");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setName("Ulyana");
        userDTO2.setSurname("Kurylina");

        Page<User> userPage = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDTO(user1)).thenReturn(userDTO1);
        when(userMapper.toDTO(user2)).thenReturn(userDTO2);

        Page<UserDTO> result = userService.getAllUsers(firstName, surname, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Ulyana", result.getContent().get(0).getName());
        assertEquals("Kurylina", result.getContent().get(0).getSurname());

        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(userMapper, times(2)).toDTO(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserWithCorrectMapping() {
        Long userId = 1L;

        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setSurname("Updated Surname");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setBirthDate(LocalDate.of(1995, 5, 5));

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setSurname("Old Surname");

        User updateEntity = new User();
        updateEntity.setName("Updated Name");
        updateEntity.setSurname("Updated Surname");
        updateEntity.setEmail("updated@example.com");
        updateEntity.setBirthDate(LocalDate.of(1995, 5, 5));

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setSurname("Updated Surname");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setBirthDate(LocalDate.of(1995, 5, 5));

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setName("Updated Name");
        updatedDTO.setSurname("Updated Surname");
        updatedDTO.setEmail("updated@example.com");
        updatedDTO.setBirthDate(LocalDate.of(1995, 5, 5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toEntity(updateDTO)).thenReturn(updateEntity);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Surname", result.getSurname());
        assertEquals("updated@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toEntity(updateDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDTO(updatedUser);
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        UserDTO updateDTO = new UserDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateDTO));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void activateOrDeactivateUser_WhenUserExists_ShouldUpdateStatus() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).updateActiveStatus(userId, false);

        userService.activateOrDeactivateUser(userId, false);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).updateActiveStatus(userId, false);
    }

    @Test
    void activateOrDeactivateUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.activateOrDeactivateUser(userId, true));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).updateActiveStatus(anyLong(), anyBoolean());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}