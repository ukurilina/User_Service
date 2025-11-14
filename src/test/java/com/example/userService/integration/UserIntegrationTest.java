package com.example.userService.integration;

import com.example.userService.dto.UserDTO;
import com.example.userService.entity.User;
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
@DisplayName("User Integration Tests")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

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
    void setUp() {
        userRepository.deleteAll();
    }

    private UserDTO createTestUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ulyana");
        userDTO.setSurname("Kurylina");
        userDTO.setEmail("ulyana.kurylina@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, UserDTO.class);
    }

    private UserDTO createUserWithEmail(String email) throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Test");
        userDTO.setSurname("User");
        userDTO.setEmail(email);
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, UserDTO.class);
    }

    @Nested
    @DisplayName("Create User Operations")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUser() throws Exception {
            UserDTO userDTO = new UserDTO();
            userDTO.setName("Ulyana");
            userDTO.setSurname("Kurylina");
            userDTO.setEmail("ulyana.new@example.com");
            userDTO.setBirthDate(LocalDate.of(1995, 5, 15));
            userDTO.setActive(true);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Ulyana")))
                    .andExpect(jsonPath("$.surname", is("Kurylina")))
                    .andExpect(jsonPath("$.email", is("ulyana.new@example.com")))
                    .andExpect(jsonPath("$.birthDate", is("1995-05-15")))
                    .andExpect(jsonPath("$.active", is(true)));

            assertEquals(1, userRepository.count(), "Should have exactly one user in database");
            User savedUser = userRepository.findAll().get(0);
            assertEquals("Ulyana", savedUser.getName());
            assertEquals("Kurylina", savedUser.getSurname());
            assertEquals("ulyana.new@example.com", savedUser.getEmail());
            assertTrue(savedUser.getActive());
        }

        @Test
        @DisplayName("Should return bad request when creating user with invalid data")
        void shouldReturnBadRequestWhenCreatingUserWithInvalidData() throws Exception {
            UserDTO invalidUser = new UserDTO();
            invalidUser.setName(""); // Empty name
            invalidUser.setSurname(""); // Empty surname
            invalidUser.setEmail("invalid-email"); // Invalid email format
            invalidUser.setBirthDate(LocalDate.now().plusDays(1)); // Future birth date
            invalidUser.setActive(true);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUser)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return bad request when creating user with duplicate email")
        void shouldReturnBadRequestWhenDuplicateEmail() throws Exception {
            createUserWithEmail("duplicate@example.com");

            UserDTO duplicateUser = new UserDTO();
            duplicateUser.setName("Another");
            duplicateUser.setSurname("User");
            duplicateUser.setEmail("duplicate@example.com"); // Same email
            duplicateUser.setBirthDate(LocalDate.of(1995, 5, 5));
            duplicateUser.setActive(true);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateUser)))
                    .andExpect(status().isBadRequest());

            assertEquals(1, userRepository.count(), "Should have exactly one user in database");
        }
    }

    @Nested
    @DisplayName("Read User Operations")
    class ReadUserTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserById() throws Exception {
            UserDTO createdUser = createTestUser();

            mockMvc.perform(get("/api/users/" + createdUser.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(createdUser.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Ulyana")))
                    .andExpect(jsonPath("$.surname", is("Kurylina")))
                    .andExpect(jsonPath("$.email", is("ulyana.kurylina@example.com")))
                    .andExpect(jsonPath("$.birthDate", is("1990-01-01")))
                    .andExpect(jsonPath("$.active", is(true)));
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void shouldReturnNotFoundWhenUserNotExists() throws Exception {
            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get all users with filtering by first name")
        void shouldGetAllUsersWithFirstNameFilter() throws Exception {
            createUserWithEmail("user1@example.com");
            createUserWithEmail("user2@example.com");

            UserDTO differentUser = new UserDTO();
            differentUser.setName("Anna");
            differentUser.setSurname("Smith");
            differentUser.setEmail("anna@example.com");
            differentUser.setBirthDate(LocalDate.of(1985, 3, 10));
            differentUser.setActive(true);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(differentUser)));

            mockMvc.perform(get("/api/users")
                            .param("firstName", "Test"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].name", is("Test")))
                    .andExpect(jsonPath("$.content[1].name", is("Test")));
        }

        @Test
        @DisplayName("Should get all users with filtering by surname")
        void shouldGetAllUsersWithSurnameFilter() throws Exception {
            createUserWithEmail("user1@example.com");

            UserDTO differentUser = new UserDTO();
            differentUser.setName("Ulyana");
            differentUser.setSurname("Kurylina");
            differentUser.setEmail("ulyana@example.com");
            differentUser.setBirthDate(LocalDate.of(1990, 1, 1));
            differentUser.setActive(true);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(differentUser)));

            mockMvc.perform(get("/api/users")
                            .param("surname", "Kurylina"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].surname", is("Kurylina")));
        }

        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() throws Exception {
            for (int i = 0; i < 5; i++) {
                createUserWithEmail("user" + i + "@example.com");
            }

            mockMvc.perform(get("/api/users")
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

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsers() throws Exception {
            mockMvc.perform(get("/api/users")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    @Nested
    @DisplayName("Update User Operations")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUser() throws Exception {
            UserDTO createdUser = createTestUser();
            createdUser.setName("Ulyana Updated");
            createdUser.setSurname("Kurylina Updated");
            createdUser.setEmail("ulyana.updated@example.com");
            createdUser.setBirthDate(LocalDate.of(1995, 5, 15));

            mockMvc.perform(put("/api/users/" + createdUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createdUser)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Ulyana Updated")))
                    .andExpect(jsonPath("$.surname", is("Kurylina Updated")))
                    .andExpect(jsonPath("$.email", is("ulyana.updated@example.com")))
                    .andExpect(jsonPath("$.birthDate", is("1995-05-15")));

            Optional<User> updatedUser = userRepository.findById(createdUser.getId());
            assertTrue(updatedUser.isPresent(), "User should exist in database");
            assertEquals("Ulyana Updated", updatedUser.get().getName());
            assertEquals("Kurylina Updated", updatedUser.get().getSurname());
            assertEquals("ulyana.updated@example.com", updatedUser.get().getEmail());
        }

        @Test
        @DisplayName("Should return not found when updating non-existent user")
        void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
            UserDTO userDTO = new UserDTO();
            userDTO.setName("Non");
            userDTO.setSurname("Existent");
            userDTO.setEmail("nonexistent@example.com");
            userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
            userDTO.setActive(true);

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return bad request when updating user with invalid data")
        void shouldReturnBadRequestWhenUpdatingUserWithInvalidData() throws Exception {
            UserDTO createdUser = createTestUser();
            createdUser.setName(""); // Empty name
            createdUser.setEmail("invalid-email"); // Invalid email

            mockMvc.perform(put("/api/users/" + createdUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createdUser)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("User Status Operations")
    class UserStatusTests {

        @Test
        @DisplayName("Should activate/deactivate user successfully")
        void shouldActivateDeactivateUser() throws Exception {
            UserDTO createdUser = createTestUser();

            mockMvc.perform(patch("/api/users/" + createdUser.getId() + "/active")
                            .param("active", "false"))
                    .andExpect(status().isOk());

            Optional<User> deactivatedUser = userRepository.findById(createdUser.getId());
            assertTrue(deactivatedUser.isPresent());
            assertFalse(deactivatedUser.get().getActive());

            mockMvc.perform(patch("/api/users/" + createdUser.getId() + "/active")
                            .param("active", "true"))
                    .andExpect(status().isOk());

            Optional<User> activatedUser = userRepository.findById(createdUser.getId());
            assertTrue(activatedUser.isPresent());
            assertTrue(activatedUser.get().getActive());
        }

        @Test
        @DisplayName("Should return not found when activating/deactivating non-existent user")
        void shouldReturnNotFoundWhenActivatingDeactivatingNonExistentUser() throws Exception {
            mockMvc.perform(patch("/api/users/999/active")
                            .param("active", "false"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete User Operations")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUser() throws Exception {
            UserDTO createdUser = createTestUser();
            assertEquals(1, userRepository.count(), "Should have one user before deletion");

            mockMvc.perform(delete("/api/users/" + createdUser.getId()))
                    .andExpect(status().isNoContent());

            assertEquals(0, userRepository.count(), "Should have no users after deletion");
            assertFalse(userRepository.existsById(createdUser.getId()), "User should not exist in database");
        }

        @Test
        @DisplayName("Should return not found when deleting non-existent user")
        void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
            mockMvc.perform(delete("/api/users/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should handle users with same name but different emails")
        void shouldHandleUsersWithSameNameDifferentEmails() throws Exception {
            UserDTO user1 = new UserDTO();
            user1.setName("Ulyana");
            user1.setSurname("Kurylina");
            user1.setEmail("ulyana1@example.com");
            user1.setBirthDate(LocalDate.of(1990, 1, 1));
            user1.setActive(true);

            UserDTO user2 = new UserDTO();
            user2.setName("Ulyana");
            user2.setSurname("Kurylina");
            user2.setEmail("ulyana2@example.com");
            user2.setBirthDate(LocalDate.of(1991, 2, 2));
            user2.setActive(true);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user2)))
                    .andExpect(status().isCreated());

            assertEquals(2, userRepository.count(), "Should have two users with same name but different emails");
        }

        @Test
        @DisplayName("Should filter users by both first name and surname")
        void shouldFilterUsersByBothFirstNameAndSurname() throws Exception {
            createUserWithEmail("test1@example.com");

            UserDTO differentUser = new UserDTO();
            differentUser.setName("Anna");
            differentUser.setSurname("Kurylina");
            differentUser.setEmail("anna@example.com");
            differentUser.setBirthDate(LocalDate.of(1985, 3, 10));
            differentUser.setActive(true);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(differentUser)));

            mockMvc.perform(get("/api/users")
                            .param("firstName", "Test")
                            .param("surname", "User"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Test")))
                    .andExpect(jsonPath("$.content[0].surname", is("User")));
        }
    }
}