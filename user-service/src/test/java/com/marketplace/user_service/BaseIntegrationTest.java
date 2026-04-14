package com.marketplace.user_service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// @SpringBootTest levanta el contexto completo de Spring para tests de integración
// webEnvironment.RANDOM_PORT inicia el servidor en un puerto aleatorio
// para evitar conflictos con la app corriendo en desarrollo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // @Container maneja el ciclo de vida del contenedor
    // static = se comparte entre todos los tests de la clase (más rápido)
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("user_test_db")
                    .withUsername("test_user")
                    .withPassword("test_pass");

    // @DynamicPropertySource sobreescribe propiedades de Spring dinámicamente
    // Se llama ANTES de que Spring inicialice el contexto
    // Así Spring usa el PostgreSQL del contenedor en vez del real
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}