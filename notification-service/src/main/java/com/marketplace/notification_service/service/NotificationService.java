package com.marketplace.notification_service.service;

import com.marketplace.notification_service.event.OrderCreatedEvent;
import com.marketplace.notification_service.event.StockReservationFailedEvent;
import com.marketplace.notification_service.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    // En producción buscaríamos el email del usuario en user-service
    // Para simplificar usamos un email fijo de prueba
    private static final String TEST_EMAIL = "test@example.com";

    public void notifyOrderCreated(OrderCreatedEvent event) {
        log.info("Sending order-created notification for order: {}", event.orderId());

        // Calculamos el total para mostrarlo en el email
        BigDecimal total = event.items().stream()
                .map(item -> item.unitPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Transformamos los items del evento a un formato simple para Thymeleaf
        List<Map<String, Object>> items = event.items().stream()
                .map(item -> Map.<String, Object>of(
                        "productName", "Producto #" + item.productId(), // simplificado
                        "quantity", item.quantity(),
                        "unitPrice", item.unitPrice()
                ))
                .toList();

        emailService.sendEmail(
                TEST_EMAIL,
                "Orden #" + event.orderId() + " recibida - Marketplace",
                "order-created",
                Map.of(
                        "orderId", event.orderId(),
                        "items", items,
                        "total", total
                )
        );
    }

    public void notifyOrderConfirmed(StockReservedEvent event) {
        log.info("Sending order-confirmed notification for order: {}", event.orderId());

        emailService.sendEmail(
                TEST_EMAIL,
                "¡Orden #" + event.orderId() + " confirmada! - Marketplace",
                "order-confirmed",
                Map.of("orderId", event.orderId())
        );
    }

    public void notifyOrderCancelled(StockReservationFailedEvent event) {
        log.info("Sending order-cancelled notification for order: {}", event.orderId());

        emailService.sendEmail(
                TEST_EMAIL,
                "Orden #" + event.orderId() + " cancelada - Marketplace",
                "order-cancelled",
                Map.of(
                        "orderId", event.orderId(),
                        "reason", event.reason()
                )
        );
    }

    public void notifyOrderPaid(Long orderId) {
        log.info("Sending order-paid notification for order: {}", orderId);

        emailService.sendEmail(
                TEST_EMAIL,
                "¡Pago confirmado para orden #" + orderId + "! - Marketplace",
                "order-confirmed",
                Map.of(
                        "orderId", orderId,
                        "message", "Tu pago fue procesado y tu orden está en camino."
                )
        );
    }
}