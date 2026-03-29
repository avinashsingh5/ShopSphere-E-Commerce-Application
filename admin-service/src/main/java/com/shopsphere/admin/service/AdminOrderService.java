package com.shopsphere.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Delegates order management operations to the Order Service.
 */
@Service
public class AdminOrderService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String ORDER_SERVICE_URL = "http://ORDER-SERVICE";

    public List<Map<String, Object>> getAllOrders(String username, String role) {
        return webClientBuilder.build()
                .get()
                .uri(ORDER_SERVICE_URL + "/orders/all")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
    }

    public Map<String, Object> updateOrderStatus(Long orderId, Map<String, Object> statusRequest,
                                                   String username, String role) {
        return webClientBuilder.build()
                .put()
                .uri(ORDER_SERVICE_URL + "/orders/" + orderId + "/status")
                .header("X-User-Email", username)
                .header("X-User-Role", role)
                .bodyValue(statusRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
