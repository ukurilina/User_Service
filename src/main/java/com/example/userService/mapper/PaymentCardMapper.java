package com.example.userService.mapper;

import com.example.userService.dto.PaymentCardDto;
import com.example.userService.entity.PaymentCard;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    PaymentCardDto toDTO(PaymentCard paymentCard);
    PaymentCard toEntity(PaymentCardDto paymentCardDTO);
}
