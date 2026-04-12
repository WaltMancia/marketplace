package com.marketplace.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "El producto es obligatorio")
        Long productId,

        @NotNull
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer quantity
) {}