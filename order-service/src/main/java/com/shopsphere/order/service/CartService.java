package com.shopsphere.order.service;

import com.shopsphere.order.dto.CartItemRequest;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.model.CartItem;
import com.shopsphere.order.repository.CartItemRepository;
import com.shopsphere.order.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder().userId(userId).build();
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public Cart addItemToCart(String userId, CartItemRequest request) {
        Cart cart = getCartByUserId(userId);

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setPrice(request.getPrice());
        } else {
            CartItem cartItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .cart(cart)
                    .build();
            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItem(String userId, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Cart cart = getCartByUserId(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeCartItem(String userId, Long itemId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}