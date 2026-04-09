package com.marketplace.productservice.dto;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description
) {}