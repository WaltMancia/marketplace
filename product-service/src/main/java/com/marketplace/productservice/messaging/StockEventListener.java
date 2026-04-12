package com.marketplace.productservice.messaging;

import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    // Escucha el evento de orden creada y descuenta el stock
    // Este es el segundo paso del Saga
    @RabbitListener(queues = "${rabbitmq.queues.order-created}")
    @Transactional
    public void handleOrderCreated(Map<String, Object> payload) {
        Long orderId = ((Number) payload.get("orderId")).longValue();
        Long userId = ((Number) payload.get("userId")).longValue();
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        log.info("Processing stock reservation for order: {}", orderId);

        try {
            // Verificamos stock de todos los items ANTES de descontar
            // Si alguno falla, no descontamos ninguno
            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException(
                                "Producto no encontrado: " + productId
                        ));

                if (product.getStock() < quantity) {
                    throw new RuntimeException(
                            "Stock insuficiente para: " + product.getName()
                                    + ". Disponible: " + product.getStock()
                                    + ", Solicitado: " + quantity
                    );
                }
            }

            // Si llegamos aquí, todos los items tienen stock
            // Descontamos el stock de cada producto
            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();

                productRepository.findById(productId).ifPresent(product -> {
                    product.setStock(product.getStock() - quantity);
                    productRepository.save(product);
                    log.info("Stock reduced for product {}: -{}", productId, quantity);
                });
            }

            // Publicamos StockReserved → order-service y notification-service
            rabbitTemplate.convertAndSend(exchange, "stock.reserved",
                    Map.of("orderId", orderId, "userId", userId));
            log.info("Stock reserved successfully for order: {}", orderId);

        } catch (Exception e) {
            log.error("Stock reservation failed for order {}: {}", orderId, e.getMessage());

            // Publicamos StockReservationFailed → Saga compensation
            rabbitTemplate.convertAndSend(exchange, "stock.reservation.failed",
                    Map.of(
                            "orderId", orderId,
                            "userId", userId,
                            "reason", e.getMessage()
                    ));
        }
    }
}