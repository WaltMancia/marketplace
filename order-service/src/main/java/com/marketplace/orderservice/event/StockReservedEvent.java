package com.marketplace.orderservice.event;

// product-service publica este evento cuando el stock se reservó exitosamente
// order-service lo consume para confirmar la orden
public record StockReservedEvent(
        Long orderId,
        Long userId
) {}