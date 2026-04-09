package com.marketplace.user_service.dto;

import java.time.LocalDateTime;

// Para endpoints que devuelven datos del usuario sin tokens
public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean isActive,
        LocalDateTime createdAt
) {}
