package com.shopsphere.order.service;

import com.shopsphere.order.dto.CheckoutRequest;
import com.shopsphere.order.dto.PaymentRequest;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.model.CartItem;
import com.shopsphere.order.model.Order;
import com.shopsphere.order.model.OrderItem;
import com.shopsphere.order.model.OrderStatus;
import com.shopsphere.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private void validateCartItemWithCatalog(CartItem cartItem) {
        try {
            Map<String, Object> product = webClientBuilder.build()
                    .get()
                    .uri("http://CATALOG-SERVICE/catalog/products/" + cartItem.getProductId())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (product == null) {
                throw new RuntimeException("Product not found: " + cartItem.getProductId());
            }

            Integer stockQuantity = (Integer) product.get("stockQuantity");
            if (stockQuantity == null || stockQuantity < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + cartItem.getProductName()
                        + ". Available: " + stockQuantity + ", Requested: " + cartItem.getQuantity());
            }

            BigDecimal catalogPrice = new BigDecimal(product.get("price").toString());
            if (catalogPrice.compareTo(cartItem.getPrice()) != 0) {
                throw new RuntimeException("Price mismatch for product: " + cartItem.getProductName()
                        + ". Catalog price: " + catalogPrice + ", Cart price: " + cartItem.getPrice());
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new RuntimeException("Product not found in catalog: " + cartItem.getProductId());
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error validating product with catalog service: " + e.getMessage());
        }
    }

    @Transactional
    public Order startCheckout(String userId, CheckoutRequest request) {
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot start checkout.");
        }

        for (CartItem item : cart.getItems()) {
            validateCartItemWithCatalog(item);
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getPrice())
                        .build())
                .collect(Collectors.toList());

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.CHECKOUT)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .items(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        return orderRepository.save(order);
    }

    @Transactional
    public Order processPayment(Long orderId, String userId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied for this order");
        }

        if (order.getStatus() != OrderStatus.CHECKOUT) {
            throw new RuntimeException("Order is not in CHECKOUT status");
        }

        order.setPaymentMode(request.getPaymentMode());
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied for this order");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Order must be paid before placing");
        }

        order.setStatus(OrderStatus.PLACED);
        order = orderRepository.save(order);

        cartService.clearCart(userId);

        return order;
    }

    public Order getOrderByIdForUser(Long orderId, String userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"ADMIN".equalsIgnoreCase(role) && !order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied for this order");
        }

        return order;
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            return orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }
}