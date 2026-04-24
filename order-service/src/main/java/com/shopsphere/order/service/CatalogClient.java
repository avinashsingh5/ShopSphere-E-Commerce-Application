package com.shopsphere.order.service;

import com.shopsphere.order.dto.ProductInfo;
import com.shopsphere.order.exception.InsufficientStockException;
import com.shopsphere.order.exception.ResourceNotFoundException;
import com.shopsphere.order.exception.ServiceCommunicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Encapsulates all inter-service calls to the Catalog Service via WebClient.
 * Translates WebClient errors into domain-specific exceptions.
 *
 * Includes X-Service-Token header for internal service-to-service authentication.
 */
@Component
public class CatalogClient {

    private static final String CATALOG_SERVICE_URL = "http://CATALOG-SERVICE";
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${service.token.secret}")
    private String serviceToken;

    /**
     * Fetches product details from Catalog Service.
     *
     * @param productId the product ID to look up
     * @return ProductInfo with trusted product data
     * @throws ResourceNotFoundException if product does not exist (404)
     * @throws ServiceCommunicationException if Catalog Service is unreachable
     */
    public ProductInfo fetchProduct(Long productId) {
        try {
            Map<String, Object> product = webClientBuilder.build()
                    .get()
                    .uri(CATALOG_SERVICE_URL + "/catalog/products/" + productId)
                    .header(SERVICE_TOKEN_HEADER, serviceToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }

            return ProductInfo.builder()
                    .id(toLong(product.get("id")))
                    .name((String) product.get("name"))
                    .price(new BigDecimal(product.get("price").toString()))
                    .stockQuantity(toInteger(product.get("stockQuantity")))
                    .build();

        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (WebClientResponseException e) {
            throw new ServiceCommunicationException(
                    "Failed to fetch product from Catalog Service: " + e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceCommunicationException(
                    "Catalog Service is unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Calls Catalog Service to reduce stock for a product.
     *
     * @param productId the product ID
     * @param quantity  the quantity to reduce
     * @throws ResourceNotFoundException if product does not exist
     * @throws InsufficientStockException if not enough stock
     * @throws ServiceCommunicationException if Catalog Service is unreachable
     */
    public void reduceStock(Long productId, Integer quantity) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri(CATALOG_SERVICE_URL + "/catalog/products/" + productId + "/reduce-stock")
                    .header(SERVICE_TOKEN_HEADER, serviceToken)
                    .bodyValue(Map.of("quantity", quantity))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (WebClientResponseException.Conflict e) {
            throw new InsufficientStockException(
                    "Insufficient stock for product id: " + productId + ". Requested: " + quantity);
        } catch (WebClientResponseException e) {
            // Check if the response body contains an "Insufficient stock" message
            String body = e.getResponseBodyAsString();
            if (body != null && body.contains("Insufficient stock")) {
                throw new InsufficientStockException(
                        "Insufficient stock for product id: " + productId + ". Requested: " + quantity);
            }
            throw new ServiceCommunicationException(
                    "Failed to reduce stock via Catalog Service: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceCommunicationException(
                    "Catalog Service is unavailable: " + e.getMessage(), e);
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
