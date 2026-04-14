package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductServiceClient;
import com.marketplace.orderservice.dto.AddToCartRequest;
import com.marketplace.orderservice.dto.CartResponse;
import com.marketplace.orderservice.entity.Cart;
import com.marketplace.orderservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartService cartService;

    private Cart emptyCart;
    private Map<String, Object> productData;

    @BeforeEach
    void setUp() {
        emptyCart = Cart.builder()
                .id(1L)
                .userId(1L)
                .build();

        productData = Map.of(
                "id", 1,
                "name", "Laptop Pro",
                "price", 999.99,
                "stock", 10,
                "sellerId", 1
        );
    }

    @Test
    @DisplayName("Should create new cart when user has none")
    void should_create_cart_when_none_exists() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        CartResponse response = cartService.getCart(1L);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should add item to cart when product has stock")
    void should_add_item_when_stock_available() {
        AddToCartRequest request = new AddToCartRequest(1L, 2);

        when(productServiceClient.getProduct(1L))
                .thenReturn(Optional.of(productData));
        when(productServiceClient.extractPrice(productData))
                .thenReturn(new BigDecimal("999.99"));
        when(productServiceClient.extractName(productData))
                .thenReturn("Laptop Pro");
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        CartResponse response = cartService.addToCart(1L, request);

        assertThat(response).isNotNull();
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw BAD_REQUEST when stock is insufficient")
    void should_throw_bad_request_when_insufficient_stock() {
        AddToCartRequest request = new AddToCartRequest(1L, 20); // pide 20, hay 10

        Map<String, Object> lowStockProduct = Map.of(
                "id", 1, "name", "Laptop", "price", 999.99, "stock", 5
        );

        when(productServiceClient.getProduct(1L))
                .thenReturn(Optional.of(lowStockProduct));

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(
                        ((ResponseStatusException) ex).getStatusCode().value()
                ).isEqualTo(400));
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when product does not exist")
    void should_throw_not_found_when_product_missing() {
        AddToCartRequest request = new AddToCartRequest(99L, 1);

        when(productServiceClient.getProduct(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(
                        ((ResponseStatusException) ex).getStatusCode().value()
                ).isEqualTo(404));
    }
}