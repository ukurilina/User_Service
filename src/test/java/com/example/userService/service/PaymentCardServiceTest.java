package com.example.userService.service;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.exception.PaymentCardNotFoundException;
import com.example.userService.exception.CardLimitExceededException;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.mapper.PaymentCardMapper;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    @Test
    void createCard_ShouldReturnCreatedCardWithCorrectMapping() {
        Long userId = 1L;

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setNumber("1234567890123456");
        cardDTO.setHolder("ULYANA KURYLINA");
        cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
        cardDTO.setActive(true);

        User user = new User();
        user.setId(userId);
        user.setName("Ulyana");

        PaymentCard cardEntity = new PaymentCard();
        cardEntity.setNumber("1234567890123456");
        cardEntity.setHolder("ULYANA KURYLINA");
        cardEntity.setExpirationDate(LocalDate.now().plusYears(2));
        cardEntity.setActive(true);

        PaymentCard savedCard = new PaymentCard();
        savedCard.setId(1L);
        savedCard.setNumber("1234567890123456");
        savedCard.setHolder("ULYANA KURYLINA");
        savedCard.setExpirationDate(LocalDate.now().plusYears(2));
        savedCard.setActive(true);
        savedCard.setUser(user);

        PaymentCardDTO savedCardDTO = new PaymentCardDTO();
        savedCardDTO.setId(1L);
        savedCardDTO.setNumber("1234567890123456");
        savedCardDTO.setHolder("ULYANA KURYLINA");
        savedCardDTO.setExpirationDate(LocalDate.now().plusYears(2));
        savedCardDTO.setActive(true);
        savedCardDTO.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(3);
        when(paymentCardMapper.toEntity(cardDTO)).thenReturn(cardEntity);
        when(paymentCardRepository.save(cardEntity)).thenReturn(savedCard);
        when(paymentCardMapper.toDTO(savedCard)).thenReturn(savedCardDTO);

        PaymentCardDTO result = paymentCardService.createCard(cardDTO, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1234567890123456", result.getNumber());
        assertEquals("ULYANA KURYLINA", result.getHolder());
        assertEquals(userId, result.getUserId());
        assertTrue(result.getActive());

        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardMapper, times(1)).toEntity(cardDTO);
        verify(paymentCardRepository, times(1)).save(cardEntity);
        verify(paymentCardMapper, times(1)).toDTO(savedCard);
    }

    @Test
    void createCard_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        PaymentCardDTO cardDTO = new PaymentCardDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentCardService.createCard(cardDTO, userId));
        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void createCard_WhenCardLimitExceeded_ShouldThrowException() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        PaymentCardDTO cardDTO = new PaymentCardDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.createCard(cardDTO, userId));
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCardWithCorrectMapping() {
        Long cardId = 1L;

        PaymentCard cardEntity = new PaymentCard();
        cardEntity.setId(cardId);
        cardEntity.setNumber("1234567890123456");
        cardEntity.setHolder("ULYANA KURYLINA");

        User user = new User();
        user.setId(1L);
        cardEntity.setUser(user);

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setId(cardId);
        cardDTO.setNumber("1234567890123456");
        cardDTO.setHolder("ULYANA KURYLINA");
        cardDTO.setUserId(1L);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(paymentCardMapper.toDTO(cardEntity)).thenReturn(cardDTO);

        PaymentCardDTO result = paymentCardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("1234567890123456", result.getNumber());
        assertEquals("ULYANA KURYLINA", result.getHolder());
        assertEquals(1L, result.getUserId());

        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardMapper, times(1)).toDTO(cardEntity);
    }

    @Test
    void getCardById_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 999L;
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> paymentCardService.getCardById(cardId));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardMapper, never()).toDTO(any());
    }

    @Test
    void getAllCards_ShouldReturnPageWithCorrectMapping() {
        Pageable pageable = PageRequest.of(0, 10);

        PaymentCard card1 = new PaymentCard();
        card1.setId(1L);
        card1.setNumber("1111222233334444");

        PaymentCard card2 = new PaymentCard();
        card2.setId(2L);
        card2.setNumber("5555666677778888");

        PaymentCardDTO cardDTO1 = new PaymentCardDTO();
        cardDTO1.setId(1L);
        cardDTO1.setNumber("1111222233334444");

        PaymentCardDTO cardDTO2 = new PaymentCardDTO();
        cardDTO2.setId(2L);
        cardDTO2.setNumber("5555666677778888");

        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card1, card2));

        when(paymentCardRepository.findAllCards(pageable)).thenReturn(cardPage);
        when(paymentCardMapper.toDTO(card1)).thenReturn(cardDTO1);
        when(paymentCardMapper.toDTO(card2)).thenReturn(cardDTO2);

        Page<PaymentCardDTO> result = paymentCardService.getAllCards(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("1111222233334444", result.getContent().get(0).getNumber());
        assertEquals("5555666677778888", result.getContent().get(1).getNumber());

        verify(paymentCardRepository, times(1)).findAllCards(pageable);
        verify(paymentCardMapper, times(2)).toDTO(any(PaymentCard.class));
    }

    @Test
    void getCardsByUserId_ShouldReturnCardsWithCorrectMapping() {
        Long userId = 1L;

        PaymentCard card1 = new PaymentCard();
        card1.setId(1L);
        card1.setNumber("1111222233334444");
        card1.setHolder("CARD HOLDER 1");

        PaymentCard card2 = new PaymentCard();
        card2.setId(2L);
        card2.setNumber("5555666677778888");
        card2.setHolder("CARD HOLDER 2");

        PaymentCardDTO cardDTO1 = new PaymentCardDTO();
        cardDTO1.setId(1L);
        cardDTO1.setNumber("1111222233334444");
        cardDTO1.setHolder("CARD HOLDER 1");

        PaymentCardDTO cardDTO2 = new PaymentCardDTO();
        cardDTO2.setId(2L);
        cardDTO2.setNumber("5555666677778888");
        cardDTO2.setHolder("CARD HOLDER 2");

        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(card1, card2));
        when(paymentCardMapper.toDTO(card1)).thenReturn(cardDTO1);
        when(paymentCardMapper.toDTO(card2)).thenReturn(cardDTO2);

        List<PaymentCardDTO> result = paymentCardService.getCardsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1111222233334444", result.get(0).getNumber());
        assertEquals("5555666677778888", result.get(1).getNumber());

        verify(paymentCardRepository, times(1)).findByUserId(userId);
        verify(paymentCardMapper, times(2)).toDTO(any(PaymentCard.class));
    }

    @Test
    void updateCard_ShouldReturnUpdatedCardWithCorrectMapping() {
        Long cardId = 1L;

        PaymentCardDTO updateDTO = new PaymentCardDTO();
        updateDTO.setNumber("9999888877776666");
        updateDTO.setHolder("UPDATED HOLDER");
        updateDTO.setExpirationDate(LocalDate.now().plusYears(3));
        updateDTO.setActive(false);

        PaymentCard existingCard = new PaymentCard();
        existingCard.setId(cardId);
        existingCard.setNumber("1111222233334444");
        existingCard.setHolder("OLD HOLDER");

        PaymentCard updateEntity = new PaymentCard();
        updateEntity.setNumber("9999888877776666");
        updateEntity.setHolder("UPDATED HOLDER");
        updateEntity.setExpirationDate(LocalDate.now().plusYears(3));
        updateEntity.setActive(false);

        PaymentCard updatedCard = new PaymentCard();
        updatedCard.setId(cardId);
        updatedCard.setNumber("9999888877776666");
        updatedCard.setHolder("UPDATED HOLDER");
        updatedCard.setExpirationDate(LocalDate.now().plusYears(3));
        updatedCard.setActive(false);

        PaymentCardDTO updatedDTO = new PaymentCardDTO();
        updatedDTO.setId(cardId);
        updatedDTO.setNumber("9999888877776666");
        updatedDTO.setHolder("UPDATED HOLDER");
        updatedDTO.setExpirationDate(LocalDate.now().plusYears(3));
        updatedDTO.setActive(false);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardMapper.toEntity(updateDTO)).thenReturn(updateEntity);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(updatedCard);
        when(paymentCardMapper.toDTO(updatedCard)).thenReturn(updatedDTO);

        PaymentCardDTO result = paymentCardService.updateCard(cardId, updateDTO);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("9999888877776666", result.getNumber());
        assertEquals("UPDATED HOLDER", result.getHolder());
        assertFalse(result.getActive());

        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardMapper, times(1)).toEntity(updateDTO);
        verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        verify(paymentCardMapper, times(1)).toDTO(updatedCard);
    }

    @Test
    void updateCard_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 999L;
        PaymentCardDTO updateDTO = new PaymentCardDTO();
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> paymentCardService.updateCard(cardId, updateDTO));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void activateOrDeactivateCard_WhenCardExists_ShouldUpdateStatus() {
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doNothing().when(paymentCardRepository).updateActiveStatus(cardId, true);

        paymentCardService.activateOrDeactivateCard(cardId, true);

        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, times(1)).updateActiveStatus(cardId, true);
    }

    @Test
    void activateOrDeactivateCard_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 999L;
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> paymentCardService.activateOrDeactivateCard(cardId, true));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, never()).updateActiveStatus(anyLong(), anyBoolean());
    }

    @Test
    void deleteCard_WhenCardExists_ShouldDeleteCard() {
        // Given
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doNothing().when(paymentCardRepository).delete(card);

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, times(1)).delete(card);
    }

    @Test
    void deleteCard_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 999L;
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> paymentCardService.deleteCard(cardId));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, never()).delete(any(PaymentCard.class));
    }
}