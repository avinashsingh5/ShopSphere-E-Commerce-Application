package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CartItemRequest;
import com.shopsphere.order.dto.CartResponse;
import com.shopsphere.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/cart")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart operations")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(summary = "Get the current user's cart")
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Email") String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart (fetches trusted product data from catalog)")
    public ResponseEntity<CartResponse> addItemToCart(
            @RequestHeader("X-User-Email") String userId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(userId, request));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update cart item quantity (re-validates against catalog)")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("id") Long id,
            @RequestParam("quantity") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, id, quantity));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<CartResponse> removeCartItem(
            @RequestHeader("X-User-Email") String userId,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(cartService.removeCartItem(userId, id));
    }
}