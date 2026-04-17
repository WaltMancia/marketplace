package com.marketplace.productservice.config;

import com.marketplace.productservice.entity.Category;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.entity.ProductStatus;
import com.marketplace.productservice.repository.CategoryRepository;
import com.marketplace.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // ID del vendedor demo — debe coincidir con el creado en user-service
    // En producción real esto vendría de una llamada al user-service
    private static final Long DEMO_SELLER_ID = 2L;

    @Override
    public void run(ApplicationArguments args) {
        seedCategories();
        seedProducts();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already seeded, skipping.");
            return;
        }

        List<Category> categories = List.of(
                Category.builder().name("Electrónica")
                        .description("Dispositivos y accesorios electrónicos").build(),
                Category.builder().name("Ropa y Moda")
                        .description("Ropa, calzado y accesorios").build(),
                Category.builder().name("Hogar y Jardín")
                        .description("Muebles, decoración y jardín").build(),
                Category.builder().name("Deportes")
                        .description("Equipamiento y ropa deportiva").build(),
                Category.builder().name("Libros")
                        .description("Libros físicos y digitales").build(),
                Category.builder().name("Juguetes")
                        .description("Juguetes y juegos para todas las edades").build());

        categoryRepository.saveAll(categories);
        log.info("✅ Seeded {} categories.", categories.size());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            log.info("Products already seeded, skipping.");
            return;
        }

        // Obtenemos las categorías recién creadas
        Category electronics = categoryRepository.findBySlug("electronica")
                .orElse(categoryRepository.findAll().get(0));
        Category fashion = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Ropa y Moda"))
                .findFirst().orElse(electronics);
        Category sports = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Deportes"))
                .findFirst().orElse(electronics);
        Category books = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Libros"))
                .findFirst().orElse(electronics);

        List<Product> products = List.of(
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(electronics)
                        .name("MacBook Pro 14 M3")
                        .description("Laptop Apple con chip M3, 16GB RAM, 512GB SSD. " +
                                "Rendimiento excepcional para profesionales.")
                        .price(new BigDecimal("1999.99"))
                        .stock(15)
                        .imageUrl("https://images.unsplash.com/photo-1517336714731" +
                                "-489689fd1ca8?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(electronics)
                        .name("Sony WH-1000XM5")
                        .description("Auriculares inalámbricos con cancelación de ruido " +
                                "líder en la industria. 30 horas de batería.")
                        .price(new BigDecimal("349.99"))
                        .stock(30)
                        .imageUrl("https://images.unsplash.com/photo-1618366712010" +
                                "-08263fc55583?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(electronics)
                        .name("iPhone 15 Pro")
                        .description("Smartphone Apple con chip A17 Pro, cámara de 48MP " +
                                "y titanio de grado aeroespacial.")
                        .price(new BigDecimal("1099.99"))
                        .stock(25)
                        .imageUrl("https://images.unsplash.com/photo-1695048133142" +
                                "-1a20484428d8?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(fashion)
                        .name("Nike Air Max 270")
                        .description("Zapatillas Nike con unidad Air Max visible. " +
                                "Diseño moderno y comodidad todo el día.")
                        .price(new BigDecimal("129.99"))
                        .stock(50)
                        .imageUrl("https://images.unsplash.com/photo-1542291026" +
                                "-7eec264c27ff?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(fashion)
                        .name("Levi's 501 Original")
                        .description("El jean icónico de Levi's. Corte recto clásico, " +
                                "denim de alta calidad.")
                        .price(new BigDecimal("79.99"))
                        .stock(75)
                        .imageUrl("https://images.unsplash.com/photo-1542272604-787c3835535d" +
                                "?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(sports)
                        .name("Kettlebell 16kg Pro")
                        .description("Kettlebell de hierro fundido con acabado antioxidante. " +
                                "Ideal para entrenamiento funcional.")
                        .price(new BigDecimal("49.99"))
                        .stock(40)
                        .imageUrl("https://images.unsplash.com/photo-1571019614242" +
                                "-c5c5dee9f50b?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(books)
                        .name("Clean Code - Robert C. Martin")
                        .description("El libro esencial sobre buenas prácticas de " +
                                "programación. Lectura obligatoria para todo desarrollador.")
                        .price(new BigDecimal("34.99"))
                        .stock(20)
                        .imageUrl("https://images.unsplash.com/photo-1544716278" +
                                "-ca5e3f4abd8c?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build(),
                Product.builder()
                        .sellerId(DEMO_SELLER_ID)
                        .category(electronics)
                        .name("iPad Air M1")
                        .description("Tablet Apple con chip M1, pantalla Liquid Retina " +
                                "de 10.9 pulgadas y 5G.")
                        .price(new BigDecimal("749.99"))
                        .stock(5) // Stock bajo para mostrar la badge de alerta
                        .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0" +
                                "?w=800&h=800&fit=crop")
                        .status(ProductStatus.ACTIVE)
                        .build());

        productRepository.saveAll(products);
        log.info("✅ Seeded {} demo products.", products.size());
    }
}