package com.shopsphere.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.admin.service.AdminProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminProductService adminProductService;

    @InjectMocks
    private AdminProductController adminProductController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminProductController).build();
    }

    @Test
    void createProduct_ShouldReturn201() throws Exception {
        Map<String, Object> request = Map.of("name", "Laptop", "price", 999.99, "stockQuantity", 10);
        Map<String, Object> response = Map.of("id", 1, "name", "Laptop");
        when(adminProductService.createProduct(any(), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/admin/products")
                        .header("X-User-Name", "admin@test.com")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteProduct_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/products/1")
                        .header("X-User-Name", "admin@test.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateProduct_ShouldReturn200() throws Exception {
        Map<String, Object> request = Map.of("name", "Updated Laptop", "price", 1099.99);
        Map<String, Object> response = Map.of("id", 1, "name", "Updated Laptop");
        when(adminProductService.updateProduct(any(), any(), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(put("/admin/products/1")
                        .header("X-User-Name", "admin@test.com")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
