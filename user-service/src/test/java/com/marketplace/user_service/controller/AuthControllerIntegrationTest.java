package com.marketplace.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.user_service.BaseIntegrationTest;
import com.marketplace.user_service.dto.LoginRequest;
import com.marketplace.user_service.dto.RegisterRequest;
import com.marketplace.user_service.repository.UserRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController Integration Tests")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends BaseIntegrationTest {

        // TestRestTemplate es el cliente HTTP para tests de integración
        // Similar a RestTemplate pero con helpers para tests
        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private MockMvc mockMvc;

        // Datos de prueba reutilizables
        private RegisterRequest validRegisterRequest;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();

                validRegisterRequest = new RegisterRequest(
                                "Juan Test",
                                "juan.test@example.com",
                                "password123",
                                "CUSTOMER");
        }

        @Nested
        @DisplayName("POST /api/v1/auth/register")
        class RegisterEndpointTests {

                @Test
                @DisplayName("Should return 201 and tokens when registration is valid")
                void should_return_201_when_valid_registration() {
                        ResponseEntity<String> response = restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        validRegisterRequest,
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.CREATED);

                        String body = response.getBody();
                        assertThat(body)
                                        .isNotNull()
                                        .contains("accessToken")
                                        .contains("refreshToken")
                                        .contains("juan.test@example.com")
                                        .doesNotContain("password"); // nunca debe aparecer el password
                }

                @Test
                @DisplayName("Should return 409 when email already registered")
                void should_return_409_when_email_duplicate() {
                        // Primer registro — debe funcionar
                        restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        validRegisterRequest,
                                        String.class);

                        // Segundo registro con el mismo email — debe fallar
                        ResponseEntity<String> response = restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        validRegisterRequest,
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT);
                }

                @Test
                @DisplayName("Should return 400 when email format is invalid")
                void should_return_400_when_invalid_email() {
                        RegisterRequest invalidRequest = new RegisterRequest(
                                        "Juan", "not-an-email", "password123", "CUSTOMER");

                        ResponseEntity<String> response = restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        invalidRequest,
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.BAD_REQUEST);
                }

                @Test
                @DisplayName("Should return 400 when password is too short")
                void should_return_400_when_password_too_short() {
                        RegisterRequest shortPass = new RegisterRequest(
                                        "Juan", "juan@test.com", "123", "CUSTOMER");

                        ResponseEntity<String> response = restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        shortPass,
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.BAD_REQUEST);
                }
        }

        @Nested
        @DisplayName("POST /api/v1/auth/login")
        class LoginEndpointTests {

                @BeforeEach
                void registerUser() {
                        // Registramos el usuario antes de los tests de login
                        restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        validRegisterRequest,
                                        String.class);
                }

                @Test
                @DisplayName("Should return 200 and tokens when credentials are valid")
                void should_return_200_with_valid_credentials() throws Exception {
                        LoginRequest loginRequest = new LoginRequest(
                                        "juan.test@example.com", "password123");

                        String responseBody = performLogin(loginRequest)
                                        .andExpect(status().isOk())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        assertThat(responseBody)
                                        .contains("accessToken")
                                        .contains("refreshToken");
                }

                @Test
                @DisplayName("Should return 401 when password is wrong")
                void should_return_401_with_wrong_password() throws Exception {
                        LoginRequest wrongPass = new LoginRequest(
                                        "juan.test@example.com", "wrongPassword");

                        performLogin(wrongPass)
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should return 401 when email does not exist")
                void should_return_401_when_email_not_found() throws Exception {
                        LoginRequest noUser = new LoginRequest(
                                        "noexiste@test.com", "password123");

                        performLogin(noUser)
                                        .andExpect(status().isUnauthorized());
                }

                private org.springframework.test.web.servlet.ResultActions performLogin(
                                LoginRequest request) throws Exception {
                        return mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/users/me")
        class GetMeTests {

                private String accessToken;

                @BeforeEach
                void loginAndGetToken() throws Exception {
                        // Registramos y luego logueamos para obtener el token
                        restTemplate.postForEntity(
                                        "/api/v1/auth/register",
                                        new RegisterRequest(
                                                        "Me Test", "me.test@example.com",
                                                        "password123", "CUSTOMER"),
                                        String.class);

                        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                                        "/api/v1/auth/login",
                                        new LoginRequest("me.test@example.com", "password123"),
                                        String.class);

                        // Extraemos el access token de la respuesta JSON
                        var jsonNode = objectMapper.readTree(loginResponse.getBody());
                        accessToken = jsonNode.get("accessToken").asText();
                }

                @Test
                @DisplayName("Should return 200 with user data when token is valid")
                void should_return_user_data_with_valid_token() {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(accessToken);

                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/v1/users/me",
                                        HttpMethod.GET,
                                        new HttpEntity<>(headers),
                                        String.class);

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody())
                                        .contains("me.test@example.com")
                                        .doesNotContain("password");
                }

                @Test
                @DisplayName("Should return 403 without token")
                void should_return_403_without_token() {
                        ResponseEntity<String> response = restTemplate.getForEntity(
                                        "/api/v1/users/me",
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                @DisplayName("Should return 403 with invalid token")
                void should_return_403_with_invalid_token() {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth("token.invalido.aqui");

                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/v1/users/me",
                                        HttpMethod.GET,
                                        new HttpEntity<>(headers),
                                        String.class);

                        assertThat(response.getStatusCode())
                                        .isEqualTo(HttpStatus.FORBIDDEN);
                }
        }
}