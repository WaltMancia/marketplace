package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductServiceClient;
import com.marketplace.orderservice.dto.AddToCartRequest;
import com.marketplace.orderservice.dto.CartResponse;
import com.marketplace.orderservice.entity.Cart;
import com.marketplace.orderservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    public CartResponse getCart(Long userId) {
        Cart cart = findOrCreateCart(userId);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // Verificamos que el producto existe y tiene stock
        Map<String, Object> productData = productServiceClient
                .getProduct(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado con id: " + request.productId()
                ));

        // Verificamos stock disponible
        Object stockObj = productData.get("stock");
        int availableStock = stockObj instanceof Number
                ? ((Number) stockObj).intValue() : 0;

        if (availableStock < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock insuficiente. Disponible: " + availableStock
            );
        }

        BigDecimal price = productServiceClient.extractPrice(productData);
        String productName = productServiceClient.extractName(productData);

        Cart cart = findOrCreateCart(userId);
        cart.addItem(request.productId(), request.quantity(), price, productName);

        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(
            Long userId,
            Long productId,
            Integer quantity
    ) {
        Cart cart = findOrCreateCart(userId);

        if (quantity <= 0) {
            cart.removeItem(productId);
        } else {
            cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado en el carrito"
                    ))
                    .setQuantity(quantity);
        }

        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        Cart cart = findOrCreateCart(userId);
        cart.removeItem(productId);
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    // findOrCreateCart implementa el patrón "get or create"
    // Si no existe carrito para el usuario, lo crea
    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).build()
                ));
    }

    private CartResponse toCartResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> new CartResponse.CartItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        // Calculamos el total sumando los subtotales de todos los items
        // BigDecimal.ZERO es el valor inicial del reduce
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), items, total);
    }
}