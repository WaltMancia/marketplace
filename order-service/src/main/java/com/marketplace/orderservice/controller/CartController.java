package com.marketplace.orderservice.controller;

import com.marketplace.orderservice.dto.AddToCartRequest;
import com.marketplace.orderservice.dto.CartResponse;
import com.marketplace.orderservice.security.MarketplaceUserDetails;
import com.marketplace.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(cartService.getCart(currentUser.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(cartService.addToCart(currentUser.getId(), request));
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
}