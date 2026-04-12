package com.marketplace.orderservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.product-service-url}")
    private String productServiceUrl;

    // Obtiene datos del producto para añadir al carrito
    public Optional<Map<String, Object>> getProduct(Long productId) {
        try {
            String url = productServiceUrl + "/api/v1/products/" + productId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    // Extrae el precio del Map de respuesta del product-service
    public BigDecimal extractPrice(Map<String, Object> productData) {
        Object priceObj = productData.get("price");
        if (priceObj instanceof Number) {
            return new BigDecimal(priceObj.toString());
        }
        return BigDecimal.ZERO;
    }

    public String extractName(Map<String, Object> productData) {
        return (String) productData.getOrDefault("name", "Producto");
    }

    public Long extractSellerId(Map<String, Object> productData) {
        Object sellerObj = productData.get("sellerId");
        if (sellerObj instanceof Number) {
            return ((Number) sellerObj).longValue();
        }
        return null;
    }
}