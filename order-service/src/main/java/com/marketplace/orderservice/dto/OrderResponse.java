package com.marketplace.orderservice.dto;

import com.marketplace.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal total,
        String shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            Long sellerId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}