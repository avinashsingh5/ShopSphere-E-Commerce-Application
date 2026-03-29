package com.shopsphere.order.service;

import com.shopsphere.order.dto.PaymentRequest;
import com.shopsphere.order.model.*;
import com.shopsphere.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .userId("user@test.com")
                .status(OrderStatus.CHECKOUT)
                .totalAmount(BigDecimal.valueOf(999.99))
                .shippingAddress("123 Main St")
                .items(new ArrayList<>())
                .build();
    }

    
    @Test
    void processPayment_ShouldUpdateStatusToPaid() {
        PaymentRequest request = new PaymentRequest("CARD");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.processPayment(1L, "user@test.com", request);

        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals("CARD", result.getPaymentMode());
    }

    
    @Test
    void processPayment_ShouldThrow_WhenNotInCheckoutStatus() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () ->
                orderService.processPayment(1L, "user@test.com", new PaymentRequest("CARD")));
    }

    @Test
    void processPayment_ShouldThrow_WhenUserMismatch() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () ->
                orderService.processPayment(1L, "wrong@test.com", new PaymentRequest("CARD")));
    }

    @Test
    void placeOrder_ShouldClearCart() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.placeOrder(1L, "user@test.com");

        verify(cartService).clearCart("user@test.com");
    }

    @Test
    void placeOrder_ShouldThrow_WhenNotPaid() {
        order.setStatus(OrderStatus.CHECKOUT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () ->
                orderService.placeOrder(1L, "user@test.com"));
    }

    @Test
    void getOrdersByUserId_ShouldReturnOrders() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByUserId("user@test.com");

        assertEquals(1, result.size());
    }

    @Test
    void updateOrderStatus_ShouldUpdateToDelivered() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "DELIVERED");

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
    }

    @Test
    void updateOrderStatus_ShouldThrow_WhenInvalidStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () ->
                orderService.updateOrderStatus(1L, "INVALID_STATUS"));
    }

    
    @Test
    void getOrderById_ShouldThrow_WhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.getOrderByIdForUser(99L, "user@test.com", "CUSTOMER"));
    }
}