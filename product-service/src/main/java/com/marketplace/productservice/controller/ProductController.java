package com.marketplace.productservice.controller;

import com.marketplace.productservice.dto.*;
import com.marketplace.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String sortBy
    ) {
        return ResponseEntity.ok(productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, sellerId, page, size, sortBy
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Necesitamos el ID del seller — viene del JWT
        // Lo extraemos del UserDetails que Spring Security inyecta
        // Por ahora usamos el email para buscar el ID — mejoraremos esto con el gateway
        Long sellerId = extractSellerId(currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(sellerId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long sellerId = extractSellerId(currentUser);
        return ResponseEntity.ok(productService.updateProduct(id, sellerId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long sellerId = extractSellerId(currentUser);
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.noContent().build();
    }

    // Extrae el sellerId del token JWT
    // En el gateway esto se manejará de forma más elegante
    private Long extractSellerId(UserDetails userDetails) {
        // El username en Spring Security es el email
        // Llamamos al user-service para obtener el ID
        // Esta es una simplificación — en producción el ID vendría directo del JWT
        return 1L; // placeholder temporal — lo completamos en el Paso 6 con el gateway
    }
}