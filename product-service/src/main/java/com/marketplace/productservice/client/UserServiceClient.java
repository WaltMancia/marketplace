package com.marketplace.productservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

// @Slf4j genera automáticamente un logger: log.info(), log.error(), etc.
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    // Verifica si un usuario existe y tiene rol de vendedor
    public boolean isValidSeller(Long sellerId) {
        try {
            String url = userServiceUrl + "/api/v1/users/internal/id/" + sellerId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String role = (String) response.getBody().get("role");
                String normalizedRole = normalizeRole(role);
                return "SELLER".equals(normalizedRole) || "ADMIN".equals(normalizedRole);
            }
            return false;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Seller not found with id: {}", sellerId);
            return false;
        } catch (Exception e) {
            // Si el user-service no está disponible, logueamos el error
            // pero NO propagamos la excepción — degradación elegante
            log.error("Error calling user-service: {}", e.getMessage());
            return false;
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole.substring("ROLE_".length()) : cleanedRole;
    }

    // Obtiene el nombre del vendedor para incluirlo en la respuesta
    public Optional<String> getSellerName(Long sellerId) {
        try {
            String url = userServiceUrl + "/api/v1/users/internal/id/" + sellerId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.ofNullable((String) response.getBody().get("name"));
            }
            return Optional.empty();

        } catch (Exception e) {
            log.warn("Could not fetch seller name for id {}: {}", sellerId, e.getMessage());
            return Optional.empty();
        }
    }
}