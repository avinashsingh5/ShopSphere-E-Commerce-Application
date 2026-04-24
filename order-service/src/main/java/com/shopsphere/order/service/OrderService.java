package com.shopsphere.order.service;

import com.shopsphere.order.dto.CheckoutRequest;
import com.shopsphere.order.dto.OrderItemResponse;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.dto.PaymentRequest;
import com.shopsphere.order.dto.ProductInfo;
import com.shopsphere.order.exception.InsufficientStockException;
import com.shopsphere.order.exception.InvalidRequestException;
import com.shopsphere.order.exception.PriceChangedException;
import com.shopsphere.order.exception.ResourceNotFoundException;
import com.shopsphere.order.exception.UnauthorizedActionException;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.model.CartItem;
import com.shopsphere.order.model.Order;
import com.shopsphere.order.model.OrderItem;
import com.shopsphere.order.model.OrderStatus;
import com.shopsphere.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CatalogClient catalogClient;

    /**
     * Validates every cart item against Catalog Service before creating the order.
     * Checks: product exists, price is current, stock is available.
     * Fails fast on any mismatch.
     */
    @Transactional
    public OrderResponse startCheckout(String userId, CheckoutRequest request) {
        Cart cart = cartService.getCartEntity(userId);

        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cart is empty. Cannot start checkout.");
        }

        // Re-validate every cart item against the catalog
        for (CartItem item : cart.getItems()) {
            ProductInfo product = catalogClient.fetchProduct(item.getProductId());

            // Check if price has changed since item was added to cart
            if (product.getPrice().compareTo(item.getPrice()) != 0) {
                throw new PriceChangedException(
                        "Price changed for product '" + product.getName()
                                + "'. Cart price: " + item.getPrice()
                                + ", Current price: " + product.getPrice()
                                + ". Please review your cart before checkout.");
            }

            // Check stock availability
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName()
                                + "'. Available: " + product.getStockQuantity()
                                + ", Requested: " + item.getQuantity());
            }
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

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse processPayment(Long orderId, String userId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied for this order");
        }

        if (order.getStatus() != OrderStatus.CHECKOUT) {
            throw new InvalidRequestException(
                    "Order is not in CHECKOUT status. Current status: " + order.getStatus());
        }

        order.setPaymentMode(request.getPaymentMode());
        order.setStatus(OrderStatus.PAID);

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    /**
     * Places the order by reducing stock for every item via Catalog Service.
     * If ANY stock reduction fails, the order stays in PAID state and an error is returned.
     * Only after ALL stock reductions succeed: order is marked PLACED and cart is cleared.
     */
    @Transactional
    public OrderResponse placeOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied for this order");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidRequestException(
                    "Order must be in PAID status before placing. Current status: " + order.getStatus());
        }

        // Reduce stock for each order item via Catalog Service
        for (OrderItem item : order.getItems()) {
            catalogClient.reduceStock(item.getProductId(), item.getQuantity());
        }

        // All stock reductions succeeded — mark order as PLACED
        order.setStatus(OrderStatus.PLACED);
        Order savedOrder = orderRepository.save(order);

        // Clear the cart only after successful placement
        cartService.clearCart(userId);

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderByIdForUser(Long orderId, String userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!"ADMIN".equalsIgnoreCase(role) && !order.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied for this order");
        }

        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            Order savedOrder = orderRepository.save(order);
            return mapToOrderResponse(savedOrder);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid order status: " + status
                    + ". Valid values: " + java.util.Arrays.toString(OrderStatus.values()));
        }
    }

    // ---- Mapping helpers ----

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMode(order.getPaymentMode())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }
}