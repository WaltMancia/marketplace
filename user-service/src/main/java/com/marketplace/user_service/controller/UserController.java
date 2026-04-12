package com.marketplace.user_service.controller;

import com.marketplace.user_service.dto.UserResponse;
import com.marketplace.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // @AuthenticationPrincipal inyecta el usuario del token JWT
    // Spring Security lo extrae del SecurityContextHolder automáticamente
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(
                userService.getUserByEmail(currentUser.getUsername())
        );
    }

    // @PreAuthorize verifica el rol ANTES de ejecutar el método
    // Solo usuarios con rol ADMIN pueden llamar este endpoint
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/internal/id/{id}")
    public ResponseEntity<UserResponse> getUserByIdInternal(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Endpoint interno — solo accesible desde otros microservicios
    // En producción esto estaría protegido por red interna, no por JWT de usuario
    @GetMapping("/internal/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}