package com.marketplace.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un carrito pertenece a un usuario — referencia cross-service
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // CascadeType.ALL → las operaciones sobre Cart se propagan a CartItem
    // orphanRemoval → si removemos un item de la lista, se borra de la BD
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default // necesario con Lombok @Builder para inicializar la lista
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Método de dominio — lógica del negocio dentro de la entidad
    // Evitamos esta lógica en el service para mantener el modelo rico
    public void addItem(Long productId, Integer quantity,
                        java.math.BigDecimal price, String productName) {
        // Si el producto ya está en el carrito, sumamos la cantidad
        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(
                                existing.getQuantity() + quantity
                        ),
                        () -> items.add(CartItem.builder()
                                .cart(this)
                                .productId(productId)
                                .productName(productName)
                                .quantity(quantity)
                                .unitPrice(price)
                                .build())
                );
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clear() {
        items.clear();
    }
}