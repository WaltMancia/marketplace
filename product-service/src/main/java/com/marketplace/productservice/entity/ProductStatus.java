package com.marketplace.productservice.entity;

public enum ProductStatus {
    ACTIVE,    // visible en el catálogo
    INACTIVE,  // oculto temporalmente por el vendedor
    DELETED    // soft delete — no se borra físicamente
}
