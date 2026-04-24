package com.shopsphere.order.service;

import com.shopsphere.order.dto.CartItemRequest;
import com.shopsphere.order.dto.CartItemResponse;
import com.shopsphere.order.dto.CartResponse;
import com.shopsphere.order.dto.ProductInfo;
import com.shopsphere.order.exception.InsufficientStockException;
import com.shopsphere.order.exception.ResourceNotFoundException;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.model.CartItem;
import com.shopsphere.order.repository.CartItemRepository;
import com.shopsphere.order.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CatalogClient catalogClient;

    public Cart getCartEntity(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder().userId(userId).build();
                    return cartRepository.save(cart);
                });
    }

    public CartResponse getCartByUserId(String userId) {
        Cart cart = getCartEntity(userId);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(String userId, CartItemRequest request) {
        // Fetch trusted product data from Catalog Service
        ProductInfo product = catalogClient.fetchProduct(request.getProductId());

        Cart cart = getCartEntity(userId);

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Validate total quantity against available stock
            if (newQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName()
                                + "'. Available: " + product.getStockQuantity()
                                + ", Requested total: " + newQuantity);
            }

            existingItem.setQuantity(newQuantity);
            existingItem.setProductName(product.getName());
            existingItem.setPrice(product.getPrice());
        } else {
            // Validate quantity against available stock
            if (request.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName()
                                + "'. Available: " + product.getStockQuantity()
                                + ", Requested: " + request.getQuantity());
            }

            CartItem cartItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .cart(cart)
                    .build();
            cart.getItems().add(cartItem);
        }

        cart = cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(String userId, Long itemId, Integer quantity) {
        Cart cart = getCartEntity(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        // Re-fetch product from Catalog Service for trusted data
        ProductInfo product = catalogClient.fetchProduct(item.getProductId());

        // Validate new quantity against available stock
        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getName()
                            + "'. Available: " + product.getStockQuantity()
                            + ", Requested: " + quantity);
        }

        item.setQuantity(quantity);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());

        cart = cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(String userId, Long itemId) {
        Cart cart = getCartEntity(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found with id: " + itemId);
        }

        cart = cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = getCartEntity(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ---- Mapping helpers ----

    public CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }
}