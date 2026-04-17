package com.marketplace.orderservice.config;

import com.marketplace.orderservice.entity.*;
import com.marketplace.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final OrderRepository orderRepository;

    private static final Long DEMO_USER_ID = 3L; // María Compradora

    @Override
    public void run(ApplicationArguments args) {
        if (orderRepository.count() > 0) {
            log.info("Orders already seeded, skipping.");
            return;
        }

        // Creamos órdenes de ejemplo con diferentes estados
        Order paidOrder = Order.builder()
                .userId(DEMO_USER_ID)
                .status(OrderStatus.PAID)
                .total(new BigDecimal("349.99"))
                .shippingAddress("Av. Principal 123, Ciudad de Guatemala")
                .build();

        OrderItem item1 = OrderItem.builder()
                .order(paidOrder)
                .productId(2L)
                .productName("Sony WH-1000XM5")
                .sellerId(2L)
                .quantity(1)
                .unitPrice(new BigDecimal("349.99"))
                .build();

        paidOrder.getItems().add(item1);

        Order deliveredOrder = Order.builder()
                .userId(DEMO_USER_ID)
                .status(OrderStatus.DELIVERED)
                .total(new BigDecimal("209.98"))
                .shippingAddress("Zona 10, Guatemala City")
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(deliveredOrder)
                .productId(4L)
                .productName("Nike Air Max 270")
                .sellerId(2L)
                .quantity(1)
                .unitPrice(new BigDecimal("129.99"))
                .build();

        OrderItem item3 = OrderItem.builder()
                .order(deliveredOrder)
                .productId(7L)
                .productName("Clean Code - Robert C. Martin")
                .sellerId(2L)
                .quantity(2)
                .unitPrice(new BigDecimal("34.99"))
                .build();

        deliveredOrder.getItems().addAll(List.of(item2, item3));

        orderRepository.saveAll(List.of(paidOrder, deliveredOrder));
        log.info("✅ Seeded {} demo orders.", 2);
    }
}