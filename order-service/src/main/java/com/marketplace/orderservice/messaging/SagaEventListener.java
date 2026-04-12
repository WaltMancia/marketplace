package com.marketplace.orderservice.messaging;

import com.marketplace.orderservice.entity.OrderStatus;
import com.marketplace.orderservice.event.StockReservationFailedEvent;
import com.marketplace.orderservice.event.StockReservedEvent;
import com.marketplace.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventListener {

    private final OrderRepository orderRepository;

    // @RabbitListener escucha mensajes de la queue especificada
    // Spring deserializa el JSON automáticamente al tipo del parámetro
    @RabbitListener(queues = "${rabbitmq.queues.stock-reserved}")
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Stock reserved for order: {}", event.orderId());

        orderRepository.findById(event.orderId()).ifPresent(order -> {
            // Avanzamos el estado de la orden en el Saga
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} confirmed after stock reservation", event.orderId());
        });
    }

    // Compensación del Saga: si el stock falla, cancelamos la orden
    @RabbitListener(queues = "${rabbitmq.queues.stock-failed}")
    @Transactional
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.warn("Stock reservation failed for order: {}. Reason: {}",
                event.orderId(), event.reason());

        orderRepository.findById(event.orderId()).ifPresent(order -> {
            // COMPENSACIÓN: revertimos la orden creada
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} cancelled due to stock failure", event.orderId());
            // En el Paso 5 notificaremos al usuario por email
        });
    }
}