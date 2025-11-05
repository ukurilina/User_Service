package com.example.userService.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@EntityListeners(AuditingEntityListener.class)
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "number")
    private String number;

    @Column(name = "holder")
    private String holder;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active")
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PaymentCard() {}

    public Long getId() {return id; }
    public void setId(Long id) {this.id = id; }

    public User getUser() {return user; }
    public void setUser(User user) {this.user = user; }

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
