package com.example.userService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class UserDto {
    private Long id;

    @NotBlank(message = "Name is necessarily")
    private String name;

    @NotBlank(message = "Surname is necessarily")
    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Email is necessarily")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Active status is necessarily")
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PaymentCardDto> paymentCards;
}