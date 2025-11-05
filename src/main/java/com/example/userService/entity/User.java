package com.example.userService.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name ="users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "active")
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCard> paymentCards = new ArrayList<>();

    public User() {}

    public void addPaymentCard(PaymentCard card) {
        if (paymentCards.size() > 5) {
            throw new IllegalStateException("User cannot have more than 5 payments cards!");
        }
        card.setUser(this);
        paymentCards.add(card);
    }

    public Long getId() {return id; }
    public void setId(Long id) {this.id = id; }

    public String getName() {return name; }
    public void setName(String name) {this.name = name; }

    public String getSurname() {return surname; }
    public void setSurname(String surname) {this.surname = surname; }

    public LocalDate getBirthDate() {return birthdate; }
    public void setBirthDate(LocalDate birthdate) {this.birthdate = birthdate; }

    public String getEmail() {return email; }
    public void setEmail(String email) {this.email = email; }

    public Boolean getActive() {return active; }
    public void setActive(Boolean active) {this.active = active; }

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; }

    public List<PaymentCard> getPaymentsCards() {return paymentCards; }
    public void setPaymentCards(List<PaymentCard> paymentCards) {this.paymentCards = paymentCards; }
}
