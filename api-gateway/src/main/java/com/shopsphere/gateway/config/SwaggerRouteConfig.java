package com.shopsphere.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerRouteConfig {

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-api-docs", r -> r
                        .path("/gateway/auth/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://AUTH-SERVICE"))
                .route("catalog-service-api-docs", r -> r
                        .path("/gateway/catalog/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://CATALOG-SERVICE"))
                .route("order-service-api-docs", r -> r
                        .path("/gateway/orders/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://ORDER-SERVICE"))
                .route("admin-service-api-docs", r -> r
                        .path("/gateway/admin/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://ADMIN-SERVICE"))
                .build();
    }
}
