package com.marketplace.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "La dirección de envío es obligatoria")
        String shippingAddress
) {}