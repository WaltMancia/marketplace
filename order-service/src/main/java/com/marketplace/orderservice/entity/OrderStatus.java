package com.marketplace.orderservice.entity;

public enum OrderStatus {
    PENDING,    // esperando confirmación de stock
    CONFIRMED,  // stock reservado, esperando pago
    PAID,       // pago confirmado
    SHIPPED,    // en camino
    DELIVERED,  // entregada
    CANCELLED   // cancelada
}