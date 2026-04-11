package com.marketplace.productservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

            // Extraemos los datos del usuario
            String username = (String) response.get("email");
            String role = (String) response.get("role");
            String normalizedRole = normalizeRole(role);

            if (username == null || normalizedRole == null) {
                throw new UsernameNotFoundException("Datos de usuario incompletos: " + email);
            }

            // Creamos la lista de autoridades (roles) del usuario
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));

            // Retornamos un UserDetails con los datos del usuario
            // Nota: La contraseña no es necesaria aquí porque usamos JWT
            return User.builder()
                    .username(username)
                    .password("") // No usamos contraseña con JWT
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading user details for email {}: {}", email, e.getMessage());
            throw new UsernameNotFoundException("Error al cargar el usuario: " + email, e);
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole.substring("ROLE_".length()) : cleanedRole;
    }
}
