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

    public Long getId() {return id; }
    public void setId(Long id) {this.id = id; }

    public Long getUserId() {return userId; }
    public void setUserId(Long userId) {this.userId = userId; }

    public String getNumber() {return number; }
    public void setNumber(String number) {this.number = number; }

    public String getHolder() {return holder; }
    public void setHolder(String holder) {this.holder = holder; }

    public LocalDate getExpirationDate() {return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) {this.expirationDate = expirationDate; }

    public Boolean getActive() {return active; }
    public void setActive(Boolean active) {this.active = active; }

    public LocalDateTime getCreatedAt() {return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() {return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; }
}
