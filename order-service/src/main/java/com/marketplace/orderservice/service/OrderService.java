package com.marketplace.orderservice.service;

import com.marketplace.orderservice.dto.CreateOrderRequest;
import com.marketplace.orderservice.dto.OrderResponse;
import com.marketplace.orderservice.dto.PageResponse;
import com.marketplace.orderservice.entity.*;
import com.marketplace.orderservice.event.OrderCreatedEvent;
import com.marketplace.orderservice.messaging.EventPublisher;
import com.marketplace.orderservice.repository.CartRepository;
import com.marketplace.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final EventPublisher eventPublisher;
    private final CartService cartService;

    @Transactional
    public OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request) {
        // Paso 1: obtenemos el carrito del usuario
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El carrito está vacío"
                ));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El carrito está vacío"
            );
        }

        // Paso 2: calculamos el total
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Paso 3: creamos la orden con status PENDING
        // En este punto NO descontamos stock — eso lo hace product-service
        // cuando recibe el evento OrderCreated
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .total(total)
                .shippingAddress(request.shippingAddress())
                .build();

        // Convertimos los items del carrito a items de la orden
        // Guardamos el precio actual — inmutable en el historial
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .sellerId(1L) // TODO: obtener del product-service
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .build())
                .toList();

        order.getItems().addAll(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Paso 4: publicamos el evento OrderCreated en RabbitMQ
        // product-service lo recibirá y descontará el stock
        // Este es el primer paso del Saga
        List<OrderCreatedEvent.OrderItemEvent> eventItems = cart.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        eventPublisher.publishOrderCreated(
                new OrderCreatedEvent(savedOrder.getId(), userId, eventItems)
        );

        // Paso 5: vaciamos el carrito DESPUÉS de crear la orden
        // Si el evento falla, el carrito sigue intacto para reintentar
        cartService.clearCart(userId);

        log.info("Order created: id={}, user={}, total={}",
                savedOrder.getId(), userId, total);

        return toOrderResponse(savedOrder);
    }

    public PageResponse<OrderResponse> getUserOrders(
            Long userId, int page, int size
    ) {
        Page<Order> ordersPage = orderRepository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new PageResponse<>(
                ordersPage.getContent().stream().map(this::toOrderResponse).toList(),
                ordersPage.getNumber(),
                ordersPage.getSize(),
                ordersPage.getTotalElements(),
                ordersPage.getTotalPages(),
                ordersPage.isLast()
        );
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orden no encontrada"
                ));
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getSellerId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotal(),
                order.getShippingAddress(),
                items,
                order.getCreatedAt()
        );
    }
}