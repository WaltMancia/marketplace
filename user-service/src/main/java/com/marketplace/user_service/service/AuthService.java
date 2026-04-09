package com.marketplace.user_service.service;

import com.marketplace.user_service.dto.*;
import com.marketplace.user_service.entity.Role;
import com.marketplace.user_service.entity.User;
import com.marketplace.user_service.repository.UserRepository;
import com.marketplace.user_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Verificamos si el email ya existe
        if (userRepository.existsByEmail(request.email())) {
            // ResponseStatusException lanza automáticamente el código HTTP correcto
            // Es la forma idiomática de Spring para manejar errores HTTP
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo electrónico ya está registrado"
            );
        }

        // Determinamos el rol — si no se especifica, es CUSTOMER por defecto
        Role role = Role.CUSTOMER;
        if (request.role() != null) {
            try {
                role = Role.valueOf(request.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Rol inválido. Valores permitidos: CUSTOMER, SELLER"
                );
            }
        }

        // Builder pattern — construimos el objeto paso a paso
        // Es más legible que un constructor con 6 parámetros
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // hasheamos
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        // authenticate() verifica email y password automáticamente
        // Si son incorrectos, lanza BadCredentialsException → 401
        // AuthenticationManager usa nuestro DaoAuthenticationProvider
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Si llegamos aquí, las credenciales son correctas
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas"
                ));

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String userEmail = jwtService.extractUsername(request.refreshToken());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Token inválido"
                ));

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expirado o inválido"
            );
        }

        // Solo generamos nuevo access token, el refresh token sigue siendo el mismo
        String newAccessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                request.refreshToken()
        );
    }

    // Método privado reutilizable para construir la respuesta
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }
}
