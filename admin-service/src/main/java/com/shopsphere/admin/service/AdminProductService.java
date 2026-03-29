package com.shopsphere.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Delegates product management operations to the Catalog Service.
 * Admin-service does NOT own product domain logic — Catalog Service is the source of truth.
 */
@Service
public class AdminProductService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String CATALOG_SERVICE_URL = "http://CATALOG-SERVICE";

    public Map<String, Object> createProduct(Map<String, Object> productRequest, String username, String role) {
        return webClientBuilder.build()
                .post()
                .uri(CATALOG_SERVICE_URL + "/catalog/products")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .bodyValue(productRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> updateProduct(Long id, Map<String, Object> productRequest, String username, String role) {
        return webClientBuilder.build()
                .put()
                .uri(CATALOG_SERVICE_URL + "/catalog/products/" + id)
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .bodyValue(productRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public void deleteProduct(Long id, String username, String role) {
        webClientBuilder.build()
                .delete()
                .uri(CATALOG_SERVICE_URL + "/catalog/products/" + id)
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public Map<String, Object> getProductsPage(String username, String role) {
        return webClientBuilder.build()
                .get()
                .uri(CATALOG_SERVICE_URL + "/catalog/products")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
