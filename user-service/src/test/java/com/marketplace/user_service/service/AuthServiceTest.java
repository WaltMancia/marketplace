package com.marketplace.user_service.service;

import com.marketplace.user_service.dto.AuthResponse;
import com.marketplace.user_service.dto.LoginRequest;
import com.marketplace.user_service.dto.RegisterRequest;
import com.marketplace.user_service.entity.Role;
import com.marketplace.user_service.entity.User;
import com.marketplace.user_service.repository.UserRepository;
import com.marketplace.user_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    // @Mock crea un mock de la clase
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    // @InjectMocks crea la instancia real e inyecta los @Mock automáticamente
    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Juan Pérez")
                .email("juan@test.com")
                .password("hashedPassword")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
    }

    // @Nested agrupa tests relacionados — mejora la legibilidad del reporte
    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully when email is new")
        void should_register_successfully_when_email_is_new() {
            // Arrange
            RegisterRequest request = new RegisterRequest(
                    "Juan Pérez", "juan@test.com", "password123", "CUSTOMER"
            );

            // cuando se llame existsByEmail → devuelve false (email libre)
            when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("juan@test.com");
            assertThat(response.accessToken()).isEqualTo("access-token");

            // Verificamos que se llamaron los métodos correctos
            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode("password123");
        }

        @Test
        @DisplayName("Should throw CONFLICT when email already exists")
        void should_throw_conflict_when_email_exists() {
            // Arrange
            RegisterRequest request = new RegisterRequest(
                    "Juan", "existing@test.com", "pass123", "CUSTOMER"
            );
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            // assertThatThrownBy verifica que se lanza una excepción específica
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value())
                                .isEqualTo(HttpStatus.CONFLICT.value());
                    });

            // Verificamos que NUNCA se intentó guardar en la BD
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should hash password before saving")
        void should_hash_password_before_saving() {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "juan@test.com", "plainPassword", "CUSTOMER"
            );
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$12$hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(any())).thenReturn("token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

            authService.register(request);

            // Capturamos el objeto que se pasó a save() para verificarlo
            // ArgumentCaptor "atrapa" el argumento de una llamada mockeada
            var captor = org.mockito.ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            // El password guardado debe ser el hasheado, no el plano
            assertThat(savedUser.getPassword()).isEqualTo("$2a$12$hashed");
            assertThat(savedUser.getPassword()).isNotEqualTo("plainPassword");
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void should_login_successfully() {
            LoginRequest request = new LoginRequest("juan@test.com", "password123");

            // authenticate() no devuelve nada útil aquí — solo verificamos que no lanza
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("juan@test.com"))
                    .thenReturn(Optional.of(testUser));
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

            AuthResponse response = authService.login(request);

            assertThat(response.email()).isEqualTo("juan@test.com");
            assertThat(response.accessToken()).isNotNull();
        }

        @Test
        @DisplayName("Should throw UNAUTHORIZED with wrong credentials")
        void should_throw_unauthorized_with_wrong_credentials() {
            LoginRequest request = new LoginRequest("juan@test.com", "wrongPass");

            // Simulamos que AuthenticationManager lanza BadCredentialsException
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            // Nunca debe buscar en la BD si la autenticación falla
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should not expose different error for wrong email vs wrong password")
        void should_use_same_error_for_wrong_email_and_wrong_password() {
            // Este test verifica que no filtramos si el email existe o no
            // Seguridad: mismo mensaje para email incorrecto y contraseña incorrecta
            LoginRequest wrongEmail = new LoginRequest("noexiste@test.com", "pass");
            LoginRequest wrongPass = new LoginRequest("juan@test.com", "wrongPass");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Ambos deben lanzar la misma excepción
            assertThatThrownBy(() -> authService.login(wrongEmail))
                    .isInstanceOf(BadCredentialsException.class);

            assertThatThrownBy(() -> authService.login(wrongPass))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}