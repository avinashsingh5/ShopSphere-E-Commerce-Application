package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CheckoutRequest;
import com.shopsphere.order.dto.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.PaymentRequest;
import com.shopsphere.order.model.Order;
import com.shopsphere.order.service.OrderService;
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
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout/start")
    public ResponseEntity<Order> startCheckout(
            @RequestHeader("X-User-Email") String userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.startCheckout(userId, request));
    }

    @PostMapping("/payment")
    public ResponseEntity<Order> processPayment(
            @RequestHeader("X-User-Email") String userId,
            @RequestParam("orderId") Long orderId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(orderService.processPayment(orderId, userId, request));
    }

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Email") String userId,
            @RequestParam("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.placeOrder(orderId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @RequestHeader("X-User-Email") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdForUser(id, userId, role));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Email") String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}