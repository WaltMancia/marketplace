package com.marketplace.orderservice.service;

import com.marketplace.orderservice.dto.CreateOrderRequest;
import com.marketplace.orderservice.dto.OrderResponse;
import com.marketplace.orderservice.entity.*;
import com.marketplace.orderservice.event.OrderCreatedEvent;
import com.marketplace.orderservice.messaging.EventPublisher;
import com.marketplace.orderservice.repository.CartRepository;
import com.marketplace.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private Cart cartWithItems;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        CartItem item = CartItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop Pro")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        cartWithItems = Cart.builder()
                .id(1L)
                .userId(1L)
                .items(List.of(item))
                .build();

        // Establecemos la referencia bidireccional
        item.setCart(cartWithItems);

        pendingOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("1999.98"))
                .shippingAddress("Calle 123")
                .items(List.of())
                .build();
    }

    @Test
    @DisplayName("Should create order and publish event when cart has items")
    void should_create_order_and_publish_event() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cartWithItems));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);
        doNothing().when(cartService).clearCart(1L);

        CreateOrderRequest request = new CreateOrderRequest("Calle 123");
        OrderResponse response = orderService.createOrderFromCart(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);

        // Verificamos que se publicó el evento de Saga
        // Este es el paso más importante del Saga Pattern
        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1))
                .publishOrderCreated(eventCaptor.capture());

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.orderId()).isEqualTo(1L);
        assertThat(publishedEvent.userId()).isEqualTo(1L);
        assertThat(publishedEvent.items()).hasSize(1);
        assertThat(publishedEvent.items().get(0).productId()).isEqualTo(1L);
        assertThat(publishedEvent.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clear cart after creating order")
    void should_clear_cart_after_order_creation() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cartWithItems));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        orderService.createOrderFromCart(1L, new CreateOrderRequest("Calle 123"));

        // Verificamos que el carrito se vació después de crear la orden
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when cart is empty")
    void should_throw_bad_request_when_cart_empty() {
        Cart emptyCart = Cart.builder().id(1L).userId(1L).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() ->
                orderService.createOrderFromCart(1L, new CreateOrderRequest("Calle 123"))
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(
                        ((ResponseStatusException) ex).getStatusCode().value()
                ).isEqualTo(400));

        // No debe publicar eventos ni guardar nada
        verify(eventPublisher, never()).publishOrderCreated(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate correct order total from cart items")
    void should_calculate_correct_total() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cartWithItems));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);
        doNothing().when(cartService).clearCart(1L);

        orderService.createOrderFromCart(1L, new CreateOrderRequest("Calle 123"));

        // Capturamos la orden que se guardó para verificar el total
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        // 2 items x $999.99 = $1999.98
        assertThat(savedOrder.getTotal())
                .isEqualByComparingTo(new BigDecimal("1999.98"));
    }
}