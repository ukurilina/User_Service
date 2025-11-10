package com.example.userService.repository;

import com.example.userService.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>{
    List <PaymentCard> findByUserId(Long userId);
    int countByUserIdAndActiveTrue(Long userId);

    @Query("SELECT pc FROM PaymentCard pc")
    Page<PaymentCard> findAllCards(Pageable pageable);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    void updateActiveStatus(@Param("id") Long id, @Param("active") Boolean active);
}
