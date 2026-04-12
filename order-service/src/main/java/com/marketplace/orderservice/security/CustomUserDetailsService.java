package com.marketplace.orderservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // Llamamos al user-service para obtener el usuario por email
            String url = userServiceUrl + "/api/v1/users/internal/" + email;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new UsernameNotFoundException("Usuario no encontrado: " + email);
            }

            Long userId = extractLong(response.get("id"));
            String username = (String) response.get("email");
            String role = (String) response.get("role");

            if (userId == null || username == null || role == null) {
                throw new UsernameNotFoundException("Datos de usuario incompletos: " + email);
            }

            return new MarketplaceUserDetails(userId, username, role);

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading user details for email {}: {}", email, e.getMessage());
            throw new UsernameNotFoundException("Error al cargar el usuario: " + email, e);
        }
    }

    private Long extractLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }
}
