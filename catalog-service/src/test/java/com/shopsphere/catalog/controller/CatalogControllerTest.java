package com.shopsphere.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.catalog.dto.ProductRequest;
import com.shopsphere.catalog.dto.ProductResponse;
import com.shopsphere.catalog.service.CategoryService;
import com.shopsphere.catalog.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CatalogController catalogController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(catalogController).build();
    }

    private ProductResponse sampleProduct() {
        return ProductResponse.builder()
                .id(1L).name("Laptop").description("Gaming laptop")
                .price(BigDecimal.valueOf(999.99)).stockQuantity(10)
                .featured(true).categoryId(1L).categoryName("Electronics").build();
    }

    @Test
    void getProducts_ShouldReturn200() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(sampleProduct()));
        when(productService.getProducts(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/catalog/products"))
                .andExpect(status().isOk());
    }

    @Test
    void getFeaturedProducts_ShouldReturn200() throws Exception {
        when(productService.getFeaturedProducts()).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(get("/catalog/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getProductById_ShouldReturn200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/catalog/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void createProduct_ShouldReturn201() throws Exception {
        ProductRequest request = new ProductRequest("Phone", "Smartphone",
                BigDecimal.valueOf(599.99), 20, null, false, 1L);
        when(productService.createProduct(any())).thenReturn(sampleProduct());

        mockMvc.perform(post("/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteProduct_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/catalog/products/1"))
                .andExpect(status().isNoContent());
    }
}
