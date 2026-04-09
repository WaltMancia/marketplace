package com.marketplace.productservice.repository;

import com.marketplace.productservice.entity.Category;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.entity.ProductStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

// Specification es una interfaz funcional que recibe Root, Query y CriteriaBuilder
// Root<Product> = la entidad principal de la query (FROM products)
// CriteriaBuilder = constructor de condiciones SQL tipadas
public class ProductSpecification {

    // Cada método devuelve una Specification que puede combinarse con .and() / .or()

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            // Join con la tabla categories para filtrar por categoría
            Join<Product, Category> categoryJoin = root.join("category");
            return cb.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            // cb.like con % = LIKE '%keyword%' en SQL
            // cb.lower hace case-insensitive
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Product> priceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice == null) return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            if (maxPrice == null) return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Product> hasSeller(Long sellerId) {
        return (root, query, cb) ->
                sellerId == null ? null : cb.equal(root.get("sellerId"), sellerId);
    }
}