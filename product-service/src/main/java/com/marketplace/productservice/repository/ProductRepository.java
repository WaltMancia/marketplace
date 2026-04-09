package com.marketplace.productservice.repository;

import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// JpaSpecificationExecutor habilita las búsquedas dinámicas con Specification
public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // Spring Data genera: SELECT * FROM products WHERE seller_id = ? AND status = ?
    Page<Product> findBySellerIdAndStatus(
            Long sellerId,
            ProductStatus status,
            Pageable pageable
    );

    Optional<Product> findBySlug(String slug);

    // Verifica si existe un producto con ese slug excluyendo un ID
    // Usado para validar slugs en actualizaciones
    boolean existsBySlugAndIdNot(String slug, Long id);
}