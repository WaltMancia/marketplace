package com.marketplace.orderservice.event;

import java.math.BigDecimal;
import java.util.List;

// Este evento se publica cuando se crea una orden
// product-service lo consume para descontar el stock
public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        List<OrderItemEvent> items
) {
    public record OrderItemEvent(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}