package com.marketplace.notification_service.event;

// product-service publica este evento cuando no hay stock suficiente
// order-service lo consume para cancelar la orden (compensación del Saga)
public record StockReservationFailedEvent(
        Long orderId,
        Long userId,
        String reason  // descripción del problema para notificar al usuario
) {}