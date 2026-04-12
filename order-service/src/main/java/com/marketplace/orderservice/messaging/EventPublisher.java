package com.marketplace.orderservice.messaging;

import com.marketplace.orderservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreated event for order: {}", event.orderId());
        // convertAndSend serializa el objeto a JSON automáticamente
        // gracias al Jackson2JsonMessageConverter que configuramos
        rabbitTemplate.convertAndSend(
                exchange,
                "order.created",  // routing key
                event
        );
    }

    public void publishOrderPaid(Long orderId, Long userId) {
        log.info("Publishing OrderPaid event for order: {}", orderId);
        rabbitTemplate.convertAndSend(
                exchange,
                "order.paid",
                new java.util.HashMap<>() {{
                    put("orderId", orderId);
                    put("userId", userId);
                }}
        );
    }
}