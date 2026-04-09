package com.marketplace.productservice.dto;

import com.marketplace.productservice.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long sellerId,
        String sellerName,      // vendrá del user-service
        Long categoryId,
        String categoryName,
        String name,
        String slug,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        ProductStatus status,
        LocalDateTime createdAt
) {}