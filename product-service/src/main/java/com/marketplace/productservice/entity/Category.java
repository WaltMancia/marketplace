package com.marketplace.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String slug;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // @OneToMany define la relación uno a muchos con Product
    // mappedBy = "category" indica que Product tiene el campo @ManyToOne
    // FetchType.LAZY → los productos NO se cargan con la categoría
    // automáticamente, solo cuando se acceden explícitamente
    // Siempre usa LAZY para evitar cargar datos innecesarios
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @ToString.Exclude  // evita StackOverflow en toString() por referencia circular
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (slug == null) {
            slug = name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
        }
    }
}