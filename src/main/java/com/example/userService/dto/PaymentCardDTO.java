package com.example.userService.dto;

import com.example.userService.entity.User;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentCardDTO {
    private Long id;

    @NotNull(message = "User ID is necessarily")
    private Long userId;

    @NotBlank(message = "Card number is necessarily")
    private String number;

    @NotBlank(message = "Card holder is necessarily")
    private String holder;

    @NotNull(message = "Expiration date is necessarily")
    private LocalDate expirationDate;

    @NotNull(message = "Active status is necessarily")
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
