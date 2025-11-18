package com.example.userService.dto;

import com.example.userService.entity.PaymentCard;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class UserDTO {
    private Long id;

    @NotBlank(message = "Name is necessarily")
    private String name;

    @NotBlank(message = "Surname is necessarily")
    private String surname;

    private LocalDate birthDate;

    @NotBlank(message = "Email is necessarily")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Active status is necessarily")
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; }

    public List<PaymentCardDTO> getPaymentsCards() {return paymentCards; }
    public void setPaymentCards(List<PaymentCardDTO> paymentCards) {this.paymentCards = paymentCards; }
}
