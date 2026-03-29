package com.shopsphere.catalog.service;

import com.shopsphere.catalog.dto.ProductRequest;
import com.shopsphere.catalog.dto.ProductResponse;
import com.shopsphere.catalog.model.Category;
import com.shopsphere.catalog.model.Product;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electronics").build();
        product = Product.builder()
                .id(1L).name("Laptop").description("Gaming laptop")
                .price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .featured(true).category(category).build();
    }

    @Test
    void getProducts_ShouldReturnPaginatedResults() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchProducts(any(), any(), any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> result = productService.getProducts(null, null, 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    void getFeaturedProducts_ShouldReturnFeaturedOnly() {
        when(productRepository.findByFeaturedTrue()).thenReturn(List.of(product));

        List<ProductResponse> result = productService.getFeaturedProducts();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isFeatured());
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertEquals("Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(999.99), result.getPrice());
    }

    @Test
    void getProductById_ShouldThrow_WhenNotExists() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_ShouldSaveAndReturn() {
        ProductRequest request = new ProductRequest("Phone", "Smartphone",
                BigDecimal.valueOf(599.99), 20, null, false, 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.createProduct(request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDelete_WhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_ShouldThrow_WhenNotExists() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> productService.deleteProduct(99L));
    }
}
