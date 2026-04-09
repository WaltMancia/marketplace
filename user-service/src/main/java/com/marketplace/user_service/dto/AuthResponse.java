package com.marketplace.user_service.dto;

// Lo que devolvemos al cliente tras login/registro exitoso
// Nunca incluimos el password aquí
public record AuthResponse(
        Long id,
        String name,
        String email,
        String role,
        String accessToken,
        String refreshToken
) {}