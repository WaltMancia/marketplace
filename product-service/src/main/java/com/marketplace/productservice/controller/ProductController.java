package com.marketplace.productservice.controller;

import com.marketplace.productservice.dto.*;
import com.marketplace.productservice.security.MarketplaceUserDetails;
import com.marketplace.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Búsqueda pública con todos los filtros como query params opcionales
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, sellerId, page, size, sortBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    // Reemplaza el método extractSellerId por lectura del header
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal UserDetails currentUser) {
        // Primero intentamos leer del header del Gateway
        // Si no hay header (llamada directa sin gateway), usamos el JWT
        Long sellerId = resolveUserId(userIdHeader, currentUser);
        boolean isAdmin = isAdmin(currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(sellerId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long sellerId = resolveUserId(userIdHeader, currentUser);
        boolean isAdmin = isAdmin(currentUser);
        return ResponseEntity.ok(productService.updateProduct(id, sellerId, isAdmin, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal UserDetails currentUser) {
        Long sellerId = resolveUserId(userIdHeader, currentUser);
        boolean isAdmin = isAdmin(currentUser);
        productService.deleteProduct(id, sellerId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // Resuelve el userId priorizando el header del Gateway
    private Long resolveUserId(
            String userIdHeader,
            UserDetails currentUser) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                // Si el header no es un número válido, usamos el JWT
            }
        }
        // Fallback: el MarketplaceUserDetails tiene el ID real
        if (currentUser instanceof MarketplaceUserDetails details) {
            return details.getId();
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "No se pudo determinar el usuario");
    }

    private boolean isAdmin(UserDetails currentUser) {
        return currentUser instanceof MarketplaceUserDetails details
                && details.hasRole("ADMIN");
    }
}