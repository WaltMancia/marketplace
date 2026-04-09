package com.marketplace.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Record de Java 21 — inmutable, con validaciones de Bean Validation
// @NotBlank, @Email, @Size son validadas automáticamente por Spring
// antes de que lleguen al service
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        // Si no se especifica rol, por defecto es CUSTOMER
        // null es válido aquí — el service lo maneja
        String role
) {}