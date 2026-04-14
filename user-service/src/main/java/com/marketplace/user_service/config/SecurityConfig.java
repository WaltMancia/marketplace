package com.marketplace.user_service.config;

import com.marketplace.user_service.repository.UserRepository;
import com.marketplace.user_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity permite usar @PreAuthorize en los controllers
// para control de acceso a nivel de método
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserRepository userRepository;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthFilter,
                        AuthenticationProvider authenticationProvider) throws Exception {
                http
                                // Desactivamos CSRF porque usamos JWT (sin cookies)
                                // CSRF es necesario con cookies de sesión, no con JWT en headers
                                .csrf(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)

                                // Configuramos qué rutas son públicas y cuáles requieren auth
                                .authorizeHttpRequests(auth -> auth
                                                // Rutas completamente públicas
                                                .requestMatchers(
                                                                "/health",
                                                                "/api/v1/auth/**", // registro, login, refresh
                                                                "/api/v1/users/internal/**")
                                                .permitAll()
                                                // Todas las demás requieren autenticación
                                                .anyRequest().authenticated())

                                // STATELESS: no usamos sesiones del servidor
                                // Cada petición debe traer su propio JWT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Registramos nuestro AuthenticationProvider
                                .authenticationProvider(authenticationProvider)

                                // Añadimos nuestro filtro ANTES del filtro de Spring Security
                                // para que se ejecute primero
                                .addFilterBefore(
                                                jwtAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                // Lambda que implementa la interfaz UserDetailsService
                // Spring Security llama a esto cuando necesita cargar un usuario
                return email -> userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Usuario no encontrado: " + email));
        }

        @Bean
        public AuthenticationProvider authenticationProvider(
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {
                // DaoAuthenticationProvider usa nuestra BD para autenticar
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                // AuthenticationManager coordina el proceso de autenticación
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                // BCrypt es el estándar para hashear contraseñas
                // El número 12 es el "strength" — cuántas rondas de hashing
                // Más alto = más seguro pero más lento
                // 12 es el balance recomendado para producción
                return new BCryptPasswordEncoder(12);
        }
}
