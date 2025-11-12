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
    private List<PaymentCardDTO> paymentCards = new ArrayList<>();

    public Long getId() {return id; }
    public void setId(Long id) {this.id = id; }

    public String getName() {return name; }
    public void setName(String name) {this.name = name; }

    public String getSurname() {return surname; }
    public void setSurname(String surname) {this.surname = surname; }

    public LocalDate getBirthDate() {return birthDate; }
    public void setBirthDate(LocalDate birthdate) {this.birthDate = birthdate; }

    public String getEmail() {return email; }
    public void setEmail(String email) {this.email = email; }

    public Boolean getActive() {return active; }
    public void setActive(Boolean active) {this.active = active; }

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; }

    public List<PaymentCardDTO> getPaymentsCards() {return paymentCards; }
    public void setPaymentCards(List<PaymentCardDTO> paymentCards) {this.paymentCards = paymentCards; }
}
