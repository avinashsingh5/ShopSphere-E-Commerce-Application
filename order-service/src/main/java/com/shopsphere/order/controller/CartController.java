package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CartItemRequest;
import com.shopsphere.order.model.Cart;
import com.shopsphere.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/cart")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestHeader("X-User-Email") String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            @RequestHeader("X-User-Email") String userId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(userId, request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<Cart> updateCartItem(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, id, quantity));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Cart> removeCartItem(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(cartService.removeCartItem(userId, id));
    }
}