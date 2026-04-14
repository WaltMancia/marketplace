package com.marketplace.user_service.service;

import com.marketplace.user_service.dto.UserResponse;
import com.marketplace.user_service.entity.Role;
import com.marketplace.user_service.entity.User;
import com.marketplace.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .name("Juan Pérez")
                .email("juan@test.com")
                .password("hashed")
                .role(Role.CUSTOMER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        inactiveUser = User.builder()
                .id(2L)
                .name("Inactivo")
                .email("inactive@test.com")
                .password("hashed")
                .role(Role.CUSTOMER)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return user response when user exists")
    void should_return_user_when_exists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.name()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when user does not exist")
    void should_throw_not_found_when_user_missing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should not expose password in response")
    void should_not_expose_password_in_response() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        UserResponse response = userService.getUserById(1L);

        // UserResponse es un record — verificamos que no tiene campo password
        // Esto asegura que el DTO nunca filtra datos sensibles
        assertThat(response).isNotNull();

        // Verificamos que el toString del record no contiene la contraseña
        assertThat(response.toString()).doesNotContain("hashed");
        assertThat(response.toString()).doesNotContain("password");
    }

    @Test
    @DisplayName("Should find user by email")
    void should_find_user_by_email() {
        when(userRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(activeUser));

        UserResponse response = userService.getUserByEmail("juan@test.com");

        assertThat(response.email()).isEqualTo("juan@test.com");
        verify(userRepository, times(1)).findByEmail("juan@test.com");
    }
}