package com.shopsphere.order.service;

import com.shopsphere.order.dto.CartItemRequest;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.model.CartItem;
import com.shopsphere.order.repository.CartItemRepository;
import com.shopsphere.order.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id(1L)
                .userId("user@test.com")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void getCartByUserId_ShouldReturnExistingCart() {
        when(cartRepository.findByUserId("user@test.com")).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUserId("user@test.com");

        assertEquals("user@test.com", result.getUserId());
    }

    @Test
    void getCartByUserId_ShouldCreateNewCart_WhenNotExists() {
        when(cartRepository.findByUserId("new@test.com")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(
                Cart.builder().id(2L).userId("new@test.com").items(new ArrayList<>()).build()
        );

        Cart result = cartService.getCartByUserId("new@test.com");

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldAddNewItem() {
        CartItemRequest request = new CartItemRequest(1L, "Laptop", 1, BigDecimal.valueOf(999.99));
        when(cartRepository.findByUserId("user@test.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItemToCart("user@test.com", request);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldUpdateQuantity_WhenProductAlreadyInCart() {
        CartItem existingItem = CartItem.builder()
                .id(1L).productId(1L).productName("Laptop")
                .quantity(1).price(BigDecimal.valueOf(999.99)).cart(cart).build();
        cart.getItems().add(existingItem);

        CartItemRequest request = new CartItemRequest(1L, "Laptop", 2, BigDecimal.valueOf(999.99));
        when(cartRepository.findByUserId("user@test.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItemToCart("user@test.com", request);

        assertEquals(3, existingItem.getQuantity()); // 1 + 2
    }

    @Test
    void removeCartItem_ShouldRemoveItem() {
        CartItem item = CartItem.builder()
                .id(1L).productId(1L).productName("Laptop")
                .quantity(1).price(BigDecimal.valueOf(999.99)).cart(cart).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId("user@test.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeCartItem("user@test.com", 1L);

        assertTrue(result.getItems().isEmpty());
    }
}
