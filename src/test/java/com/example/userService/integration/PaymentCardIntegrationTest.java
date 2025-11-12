package com.example.userService.integration;

import com.example.userService.dto.PaymentCardDTO;
import com.example.userService.dto.UserDTO;
import com.example.userService.entity.PaymentCard;
import com.example.userService.entity.User;
import com.example.userService.repository.PaymentCardRepository;
import com.example.userService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Payment Card Integration Tests")
public class PaymentCardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @BeforeEach
    void setUp() throws Exception {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ulyana");
        userDTO.setSurname("Kurylina");
        userDTO.setEmail("ulyana.doe@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        UserDTO createdUser = objectMapper.readValue(response, UserDTO.class);
        userId = createdUser.getId();
        assertNotNull(userId, "User ID should not be null after creation");
    }

    private PaymentCardDTO createTestCard() throws Exception {
        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setNumber("4111111111111111");
        cardDTO.setHolder("ULYANA KURYLINA");
        cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
        cardDTO.setActive(true);
        cardDTO.setUserId(userId);

        String response = mockMvc.perform(post("/api/payment_cards/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, PaymentCardDTO.class);
    }

    private PaymentCardDTO createCardWithNumber(String cardNumber) throws Exception {
        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setNumber(cardNumber);
        cardDTO.setHolder("ULYANA KURYLINA");
        cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
        cardDTO.setActive(true);
        cardDTO.setUserId(userId);

        String response = mockMvc.perform(post("/api/payment_cards/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, PaymentCardDTO.class);
    }

    @Nested
    @DisplayName("Create Card Operations")
    class CreateCardTests {

        @Test
        @DisplayName("Should create card successfully with valid data")
        void shouldCreateCard() throws Exception {
            PaymentCardDTO cardDTO = new PaymentCardDTO();
            cardDTO.setNumber("5555444433332222");
            cardDTO.setHolder("ULYANA KURYLINA");
            cardDTO.setExpirationDate(LocalDate.now().plusYears(3));
            cardDTO.setActive(true);
            cardDTO.setUserId(userId);

            mockMvc.perform(post("/api/payment_cards/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cardDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.number", is("5555444433332222")))
                    .andExpect(jsonPath("$.holder", is("ULYANA KURYLINA")))
                    .andExpect(jsonPath("$.active", is(true)))
                    .andExpect(jsonPath("$.userId", is(userId.intValue())));

            assertEquals(1, paymentCardRepository.count(), "Should have exactly one card in database");
            PaymentCard savedCard = paymentCardRepository.findAll().get(0);
            assertEquals("5555444433332222", savedCard.getNumber());
            assertEquals("ULYANA KURYLINA", savedCard.getHolder());
            assertTrue(savedCard.getActive());
        }

        @Test
        @DisplayName("Should return bad request when creating card with invalid data")
        void shouldReturnBadRequestWhenCreatingCardWithInvalidData() throws Exception {
            PaymentCardDTO invalidCard = new PaymentCardDTO();
            invalidCard.setNumber("");
            invalidCard.setHolder("");
            invalidCard.setExpirationDate(LocalDate.now().minusDays(1)); // Past date
            invalidCard.setActive(true);
            invalidCard.setUserId(userId);

            mockMvc.perform(post("/api/payment_cards/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCard)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return not found when creating card for non-existent user")
        void shouldReturnNotFoundWhenUserNotExists() throws Exception {
            PaymentCardDTO cardDTO = new PaymentCardDTO();
            cardDTO.setNumber("4111111111111111");
            cardDTO.setHolder("ULYANA KURYLINA");
            cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
            cardDTO.setActive(true);
            cardDTO.setUserId(999L); // Non-existent user ID

            mockMvc.perform(post("/api/payment_cards/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cardDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return bad request when exceeding card limit")
        void shouldNotExceedCardLimit() throws Exception {
            for (int i = 0; i < 5; i++) {
                createCardWithNumber("411111111111111" + i);
            }

            PaymentCardDTO sixthCard = new PaymentCardDTO();
            sixthCard.setNumber("5111111111111111");
            sixthCard.setHolder("KURYLINA ULYANA");
            sixthCard.setExpirationDate(LocalDate.now().plusYears(2));
            sixthCard.setActive(true);
            sixthCard.setUserId(userId);

            mockMvc.perform(post("/api/payment_cards/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sixthCard)))
                    .andExpect(status().isBadRequest());

            assertEquals(5, paymentCardRepository.count(), "Should have exactly 5 cards in database");
        }
    }

    @Nested
    @DisplayName("Read Card Operations")
    class ReadCardTests {

        @Test
        @DisplayName("Should get card by ID successfully")
        void shouldGetCardById() throws Exception {
            PaymentCardDTO createdCard = createTestCard();

            mockMvc.perform(get("/api/payment_cards/" + createdCard.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(createdCard.getId().intValue())))
                    .andExpect(jsonPath("$.number", is("4111111111111111")))
                    .andExpect(jsonPath("$.holder", is("ULYANA KURYLINA")))
                    .andExpect(jsonPath("$.active", is(true)))
                    .andExpect(jsonPath("$.userId", is(userId.intValue())));
        }

        @Test
        @DisplayName("Should return not found when card does not exist")
        void shouldReturnNotFoundWhenCardNotExists() throws Exception {
            mockMvc.perform(get("/api/payment_cards/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get all cards by user ID")
        void shouldGetAllCardsByUserId() throws Exception {
            createCardWithNumber("4111111111111111");
            createCardWithNumber("5111111111111111");
            createCardWithNumber("3711111111111111");

            mockMvc.perform(get("/api/payment_cards/users/" + userId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].number", is("4111111111111111")))
                    .andExpect(jsonPath("$[1].number", is("5111111111111111")))
                    .andExpect(jsonPath("$[2].number", is("3711111111111111")))
                    .andExpect(jsonPath("$[0].userId", is(userId.intValue())))
                    .andExpect(jsonPath("$[1].userId", is(userId.intValue())))
                    .andExpect(jsonPath("$[2].userId", is(userId.intValue())));
        }

        @Test
        @DisplayName("Should return empty list when user has no cards")
        void shouldReturnEmptyListWhenUserHasNoCards() throws Exception {
            mockMvc.perform(get("/api/payment_cards/users/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should get all cards with pagination")
        void shouldGetAllCardsWithPagination() throws Exception {
            for (int i = 0; i < 5; i++) {
                createCardWithNumber("411111111111111" + i);
            }

            mockMvc.perform(get("/api/payment_cards")
                            .param("page", "0")
                            .param("size", "3"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(5)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.number", is(0)))
                    .andExpect(jsonPath("$.size", is(3)));
        }
    }

    @Nested
    @DisplayName("Update Card Operations")
    class UpdateCardTests {

        @Test
        @DisplayName("Should update card successfully")
        void shouldUpdateCard() throws Exception {
            PaymentCardDTO createdCard = createTestCard();
            createdCard.setNumber("7777888899990000");
            createdCard.setHolder("UPDATED HOLDER");
            createdCard.setExpirationDate(LocalDate.now().plusYears(4));
            createdCard.setActive(false);

            mockMvc.perform(put("/api/payment_cards/" + createdCard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createdCard)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number", is("7777888899990000")))
                    .andExpect(jsonPath("$.holder", is("UPDATED HOLDER")))
                    .andExpect(jsonPath("$.active", is(false)));

            Optional<PaymentCard> updatedCard = paymentCardRepository.findById(createdCard.getId());
            assertTrue(updatedCard.isPresent(), "Card should exist in database");
            assertEquals("7777888899990000", updatedCard.get().getNumber());
            assertEquals("UPDATED HOLDER", updatedCard.get().getHolder());
            assertFalse(updatedCard.get().getActive());
        }

        @Test
        @DisplayName("Should return not found when updating non-existent card")
        void shouldReturnNotFoundWhenUpdatingNonExistentCard() throws Exception {
            PaymentCardDTO cardDTO = new PaymentCardDTO();
            cardDTO.setNumber("4111111111111111");
            cardDTO.setHolder("ULYANA KURYLINA");
            cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
            cardDTO.setActive(true);
            cardDTO.setUserId(userId);

            // When & Then
            mockMvc.perform(put("/api/payment_cards/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cardDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should activate/deactivate card successfully")
        void shouldActivateDeactivateCard() throws Exception {
            // Given
            PaymentCardDTO createdCard = createTestCard();

            mockMvc.perform(patch("/api/payment_cards/" + createdCard.getId() + "/active")
                            .param("active", "false"))
                    .andExpect(status().isOk());

            Optional<PaymentCard> deactivatedCard = paymentCardRepository.findById(createdCard.getId());
            assertTrue(deactivatedCard.isPresent());
            assertFalse(deactivatedCard.get().getActive());

            mockMvc.perform(patch("/api/payment_cards/" + createdCard.getId() + "/active")
                            .param("active", "true"))
                    .andExpect(status().isOk());

            Optional<PaymentCard> activatedCard = paymentCardRepository.findById(createdCard.getId());
            assertTrue(activatedCard.isPresent());
            assertTrue(activatedCard.get().getActive());
        }
    }

    @Nested
    @DisplayName("Delete Card Operations")
    class DeleteCardTests {

        @Test
        @DisplayName("Should delete card successfully")
        void shouldDeleteCard() throws Exception {
            PaymentCardDTO createdCard = createTestCard();
            assertEquals(1, paymentCardRepository.count(), "Should have one card before deletion");

            mockMvc.perform(delete("/api/payment_cards/" + createdCard.getId()))
                    .andExpect(status().isNoContent());

            assertEquals(0, paymentCardRepository.count(), "Should have no cards after deletion");
            assertFalse(paymentCardRepository.existsById(createdCard.getId()), "Card should not exist in database");
        }

        @Test
        @DisplayName("Should return not found when deleting non-existent card")
        void shouldReturnNotFoundWhenDeletingNonExistentCard() throws Exception {
            mockMvc.perform(delete("/api/payment_cards/999"))
                    .andExpect(status().isNotFound());
        }
    }
}