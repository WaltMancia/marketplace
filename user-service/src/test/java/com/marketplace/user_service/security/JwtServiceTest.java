package com.marketplace.user_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// @ExtendWith(MockitoExtension.class) activa Mockito en JUnit 5
// Equivale a llamar MockitoAnnotations.openMocks(this) manualmente
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // ReflectionTestUtils inyecta valores en campos privados
        // sin necesidad de setters — útil para @Value en tests unitarios
        ReflectionTestUtils.setField(
                jwtService, "secretKey",
                "testSecretKeyMustBe256BitsLongForHmacSha256Algorithm1234567890"
        );
        ReflectionTestUtils.setField(
                jwtService, "accessTokenExpiration", 900000L
        );
        ReflectionTestUtils.setField(
                jwtService, "refreshTokenExpiration", 604800000L
        );

        testUser = User.builder()
                .username("test@test.com")
                .password("hashedPassword")
                .roles("CUSTOMER")
                .build();
    }

    @Test
    @DisplayName("Should generate valid access token")
    void should_generate_valid_access_token() {
        // Arrange — ya hecho en setUp

        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtService.extractUsername(token))
                .isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Should validate access token correctly")
    void should_validate_access_token() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("Should reject refresh token as access token")
    void should_reject_refresh_token_as_access_token() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Un refresh token NO debe ser válido como access token
        assertThat(jwtService.isTokenValid(refreshToken, testUser)).isFalse();
    }

    @Test
    @DisplayName("Should validate refresh token correctly")
    void should_validate_refresh_token() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertThat(jwtService.isRefreshTokenValid(refreshToken, testUser)).isTrue();
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void should_extract_username_from_token() {
        String token = jwtService.generateAccessToken(testUser);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@test.com");
    }
}