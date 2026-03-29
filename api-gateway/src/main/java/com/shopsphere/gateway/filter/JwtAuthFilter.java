package com.shopsphere.gateway.filter;

import com.shopsphere.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> OPEN_EXACT = List.of(
            "/gateway/auth/login",
            "/gateway/auth/signup",
            "/swagger-ui.html"
    );

    private static final List<String> OPEN_PREFIXES = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/swagger-resources",
            "/gateway/auth/v3/api-docs",
            "/gateway/auth/swagger-ui",
            "/gateway/catalog/v3/api-docs",
            "/gateway/catalog/swagger-ui",
            "/gateway/orders/v3/api-docs",
            "/gateway/orders/swagger-ui",
            "/gateway/admin/v3/api-docs",
            "/gateway/admin/swagger-ui"
    );

    private static final List<String> OPEN_GET_PREFIXES = List.of(
            "/gateway/catalog/products",
            "/gateway/catalog/featured",
            "/gateway/catalog/categories"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        HttpMethod method = request.getMethod();

        if (isOpenEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            jwtUtil.validateToken(token);

            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if ((path.startsWith("/gateway/admin") || path.startsWith("/gateway/auth/admin"))
                    && !"ADMIN".equalsIgnoreCase(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .headers(headers -> {
                        headers.set("X-User-Email", email);
                        headers.set("X-User-Role", role);
                    })
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isOpenEndpoint(String path, HttpMethod method) {
        for (String open : OPEN_EXACT) {
            if (path.equals(open)) {
                return true;
            }
        }

        for (String prefix : OPEN_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?")) {
                return true;
            }
        }

        if (method == HttpMethod.GET) {
            for (String prefix : OPEN_GET_PREFIXES) {
                if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}