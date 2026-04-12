package com.marketplace.orderservice.controller;

import com.marketplace.orderservice.dto.CreateOrderRequest;
import com.marketplace.orderservice.dto.OrderResponse;
import com.marketplace.orderservice.dto.PageResponse;
import com.marketplace.orderservice.security.MarketplaceUserDetails;
import com.marketplace.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.createOrderFromCart(currentUser.getId(), request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(orderService.getUserOrders(currentUser.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal MarketplaceUserDetails currentUser
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id, currentUser.getId()));
    }
}