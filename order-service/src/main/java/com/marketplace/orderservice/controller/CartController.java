package com.marketplace.orderservice.controller;

import com.marketplace.orderservice.dto.AddToCartRequest;
import com.marketplace.orderservice.dto.CartResponse;
import com.marketplace.orderservice.security.MarketplaceUserDetails;
import com.marketplace.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // En CartController.java
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long userId = extractUserId(userIdHeader, currentUser);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long userId = extractUserId(userIdHeader, currentUser);
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(currentUser.getId(), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(cartService.removeFromCart(currentUser.getId(), productId));
    }

    private Long extractUserId(
            String userIdHeader,
            UserDetails currentUser
    ) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException ignored) {}
        }
        if (currentUser instanceof MarketplaceUserDetails details) {
            return details.getId();
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "No se pudo determinar el usuario"
        );
    }
}