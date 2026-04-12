package com.marketplace.orderservice.dto;

import java.util.List;

// DTO genérico para respuestas paginadas
// Equivale al PaginatedResponse que hicimos en Python
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}