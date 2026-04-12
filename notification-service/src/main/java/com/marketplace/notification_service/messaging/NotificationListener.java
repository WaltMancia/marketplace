package com.marketplace.notification_service.messaging;

import com.marketplace.notification_service.event.OrderCreatedEvent;
import com.marketplace.notification_service.event.StockReservationFailedEvent;
import com.marketplace.notification_service.event.StockReservedEvent;
import com.marketplace.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    // Este listener recibe el mismo evento que publica order-service
    // RabbitMQ deserializa el JSON al tipo del parámetro automáticamente
    @RabbitListener(queues = "${rabbitmq.queues.order-created}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event for order: {}", event.orderId());
        try {
            notificationService.notifyOrderCreated(event);
        } catch (Exception e) {
            // Si falla, RabbitMQ enviará el mensaje a la DLQ
            // después de los reintentos configurados
            log.error("Failed to process OrderCreated notification: {}", e.getMessage());
            throw e; // relanzamos para que RabbitMQ sepa que falló
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.stock-reserved}")
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received StockReserved event for order: {}", event.orderId());
        try {
            notificationService.notifyOrderConfirmed(event);
        } catch (Exception e) {
            log.error("Failed to process StockReserved notification: {}", e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.stock-failed}")
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.warn("Received StockReservationFailed event for order: {}", event.orderId());
        try {
            notificationService.notifyOrderCancelled(event);
        } catch (Exception e) {
            log.error("Failed to process StockFailed notification: {}", e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-paid}")
    public void handleOrderPaid(Map<String, Object> payload) {
        log.info("Received OrderPaid event: {}", payload);
        try {
            Long orderId = ((Number) payload.get("orderId")).longValue();
            notificationService.notifyOrderPaid(orderId);
        } catch (Exception e) {
            log.error("Failed to process OrderPaid notification: {}", e.getMessage());
            throw e;
        }
    }
}