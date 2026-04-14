package com.marketplace.productservice.service;

import com.marketplace.productservice.client.UserServiceClient;
import com.marketplace.productservice.dto.*;
import com.marketplace.productservice.entity.Category;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.entity.ProductStatus;
import com.marketplace.productservice.repository.ProductRepository;
import com.marketplace.productservice.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepository;
        private final CategoryService categoryService;
        private final UserServiceClient userServiceClient;

        // @Transactional garantiza que todo el método es atómico
        @Transactional
        public ProductResponse createProduct(Long sellerId, ProductRequest request) {
                if (!userServiceClient.isValidSeller(sellerId)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permiso para crear productos");
                }

                Category category = categoryService.getCategoryEntityById(request.categoryId());

                Product product = Product.builder()
                                .sellerId(sellerId)
                                .category(category)
                                .name(request.name())
                                .description(request.description())
                                .price(request.price())
                                .stock(request.stock())
                                .imageUrl(request.imageUrl())
                                .status(ProductStatus.ACTIVE)
                                .build();

                Product saved = productRepository.save(product);
                log.info("Product created: id={}, seller={}", saved.getId(), sellerId);

                return toResponse(saved);
        }

        // Búsqueda paginada con filtros opcionales
        public PageResponse<ProductResponse> searchProducts(
                        String keyword,
                        Long categoryId,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Long sellerId,
                        int page,
                        int size,
                        String sortBy) {
                // Construimos la Specification combinando todos los filtros
                // Specification.where(null) es el punto de partida neutral
                Specification<Product> spec = Specification
                                .where(ProductSpecification.hasStatus(ProductStatus.ACTIVE))
                                .and(ProductSpecification.nameContains(keyword))
                                .and(ProductSpecification.hasCategory(categoryId))
                                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                                .and(ProductSpecification.hasSeller(sellerId));

                // Pageable encapsula paginación y ordenación
                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt"));

                Page<Product> productPage = productRepository.findAll(spec, pageable);

                return new PageResponse<>(
                                productPage.getContent().stream().map(this::toResponse).toList(),
                                productPage.getNumber(),
                                productPage.getSize(),
                                productPage.getTotalElements(),
                                productPage.getTotalPages(),
                                productPage.isLast());
        }

        public ProductResponse getProductById(Long id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Producto no encontrado"));
                return toResponse(product);
        }

        public ProductResponse getProductBySlug(String slug) {
                Product product = productRepository.findBySlug(slug)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Producto no encontrado"));
                return toResponse(product);
        }

        @Transactional
        public ProductResponse updateProduct(
                        Long productId,
                        Long sellerId,
                        ProductRequest request) {
                return updateProduct(productId, sellerId, false, request);
        }

        @Transactional
        public ProductResponse updateProduct(
                        Long productId,
                        Long sellerId,
                        boolean isAdmin,
                        ProductRequest request) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Producto no encontrado"));

                // Verificamos que el producto pertenece al vendedor que lo edita
                if (!isAdmin && !product.getSellerId().equals(sellerId)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permiso para editar este producto");
                }

                Category category = categoryService.getCategoryEntityById(request.categoryId());

                product.setCategory(category);
                product.setName(request.name());
                product.setDescription(request.description());
                product.setPrice(request.price());
                product.setStock(request.stock());
                product.setImageUrl(request.imageUrl());

                return toResponse(productRepository.save(product));
        }

        @Transactional
        public void deleteProduct(Long productId, Long sellerId) {
                deleteProduct(productId, sellerId, false);
        }

        @Transactional
        public void deleteProduct(Long productId, Long sellerId, boolean isAdmin) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Producto no encontrado"));

                if (!isAdmin && !product.getSellerId().equals(sellerId)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permiso para eliminar este producto");
                }

                // Soft delete — cambiamos el status en vez de borrar físicamente
                product.setStatus(ProductStatus.DELETED);
                productRepository.save(product);
                log.info("Product soft-deleted: id={}", productId);
        }

        // Mapeo de entidad a DTO — obtiene el nombre del seller del user-service
        private ProductResponse toResponse(Product product) {
                String sellerName = userServiceClient
                                .getSellerName(product.getSellerId())
                                .orElse("Vendedor desconocido");

                return new ProductResponse(
                                product.getId(),
                                product.getSellerId(),
                                sellerName,
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getName(),
                                product.getSlug(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getStock(),
                                product.getImageUrl(),
                                product.getStatus(),
                                product.getCreatedAt());
        }
}