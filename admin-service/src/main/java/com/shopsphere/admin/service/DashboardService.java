package com.shopsphere.admin.service;

import com.shopsphere.admin.dto.DashboardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Aggregates data from Catalog and Order services for dashboard metrics.
 */
@Service
public class DashboardService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String CATALOG_SERVICE_URL = "http://CATALOG-SERVICE";
    private static final String ORDER_SERVICE_URL = "http://ORDER-SERVICE";

    public DashboardResponse getDashboardMetrics(String username, String role) {
        // Get product count from catalog service
        Map<String, Object> productsPage = webClientBuilder.build()
                .get()
                .uri(CATALOG_SERVICE_URL + "/catalog/products?size=1")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        long totalProducts = productsPage != null && productsPage.get("totalElements") != null
                ? ((Number) productsPage.get("totalElements")).longValue() : 0;

        // Get all orders from order service
        List<Map<String, Object>> orders = webClientBuilder.build()
                .get()
                .uri(ORDER_SERVICE_URL + "/orders/all")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        long totalOrders = orders != null ? orders.size() : 0;
        long pendingOrders = orders != null ? orders.stream()
                .filter(o -> {
                    String status = (String) o.get("status");
                    return "PLACED".equals(status) || "PAID".equals(status) || "PACKED".equals(status) || "SHIPPED".equals(status);
                }).count() : 0;
        long deliveredOrders = orders != null ? orders.stream()
                .filter(o -> "DELIVERED".equals(o.get("status"))).count() : 0;
        long cancelledOrders = orders != null ? orders.stream()
                .filter(o -> "CANCELLED".equals(o.get("status"))).count() : 0;

        return DashboardResponse.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }
}
