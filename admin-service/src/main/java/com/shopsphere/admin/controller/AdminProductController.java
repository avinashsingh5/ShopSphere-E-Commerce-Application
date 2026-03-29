package com.shopsphere.admin.controller;

import com.shopsphere.admin.service.AdminProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductService.createProduct(productRequest, username, role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> productRequest) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, productRequest, username, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role) {
        adminProductService.deleteProduct(id, username, role);
        return ResponseEntity.noContent().build();
    }
}
