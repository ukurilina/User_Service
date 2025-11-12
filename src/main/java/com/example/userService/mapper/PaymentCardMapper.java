package com.example.userService.mapper;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.entity.PaymentCard;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    PaymentCardDTO toDTO(PaymentCard paymentCard);
    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);
}
