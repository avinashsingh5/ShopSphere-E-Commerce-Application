package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CheckoutRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.dto.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.PaymentRequest;
import com.shopsphere.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Checkout, payment, order placement & management")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Start checkout — validates cart items against catalog and creates order")
    public ResponseEntity<OrderResponse> startCheckout(
            @RequestHeader("X-User-Email") String userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.startCheckout(userId, request));
    }

    @PostMapping("/{orderId}/payment")
    @Operation(summary = "Process payment for an order in CHECKOUT status")
    public ResponseEntity<OrderResponse> processPayment(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(orderService.processPayment(orderId, userId, request));
    }

    @PostMapping("/{orderId}/place")
    @Operation(summary = "Place the order — reduces stock via Catalog Service and finalizes")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.placeOrder(orderId, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (own orders or admin)")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("X-User-Email") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdForUser(id, userId, role));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all orders for the current user")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("X-User-Email") String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin only)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}