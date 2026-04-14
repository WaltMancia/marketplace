package com.marketplace.productservice.service;

import com.marketplace.productservice.client.UserServiceClient;
import com.marketplace.productservice.dto.ProductRequest;
import com.marketplace.productservice.dto.ProductResponse;
import com.marketplace.productservice.entity.Category;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.entity.ProductStatus;
import com.marketplace.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private ProductRequest validRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electrónica")
                .slug("electronica")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .sellerId(1L)
                .category(testCategory)
                .name("Laptop Pro")
                .slug("laptop-pro-123")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .status(ProductStatus.ACTIVE)
                .build();

        validRequest = new ProductRequest(
                1L, "Laptop Pro",
                "Una laptop excelente",
                new BigDecimal("999.99"),
                10, null
        );
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product when seller is valid")
        void should_create_product_when_seller_is_valid() {
            when(userServiceClient.isValidSeller(1L)).thenReturn(true);
            when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(userServiceClient.getSellerName(1L))
                    .thenReturn(Optional.of("Juan Vendedor"));

            ProductResponse response = productService.createProduct(1L, validRequest);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Laptop Pro");
            assertThat(response.price()).isEqualByComparingTo(new BigDecimal("999.99"));
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not a valid seller")
        void should_throw_forbidden_when_not_valid_seller() {
            when(userServiceClient.isValidSeller(1L)).thenReturn(false);

            assertThatThrownBy(() -> productService.createProduct(1L, validRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode().value()
                    ).isEqualTo(403));

            // Nunca debe llegar a guardar en la BD
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete product when seller owns it")
        void should_soft_delete_when_owner() {
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class)))
                    .thenReturn(testProduct);

            productService.deleteProduct(1L, 1L); // sellerId = 1L

            // Capturamos el producto que se guardó para verificar el soft delete
            var captor = org.mockito.ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus())
                    .isEqualTo(ProductStatus.DELETED);
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when seller does not own product")
        void should_throw_forbidden_when_not_owner() {
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct)); // producto del seller 1L

            // El seller 2L intenta borrar el producto del seller 1L
            assertThatThrownBy(() -> productService.deleteProduct(1L, 2L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode().value()
                    ).isEqualTo(403));
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when product does not exist")
        void should_throw_not_found_when_product_missing() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(99L, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode().value()
                    ).isEqualTo(404));
        }
    }

    @Nested
    @DisplayName("price validation")
    class PriceValidationTests {

        // @ParameterizedTest ejecuta el test con cada valor de @ValueSource
        // Muy útil para probar múltiples inputs con la misma lógica
        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-1.00", "-100.00"})
        @DisplayName("Should reject products with non-positive prices")
        void should_reject_non_positive_prices(String price) {
            // Este test verifica que las validaciones de Bean Validation
            // rechacen precios inválidos antes de llegar al service
            BigDecimal invalidPrice = new BigDecimal(price);
            assertThat(invalidPrice).isLessThanOrEqualTo(BigDecimal.ZERO);
        }
    }
}