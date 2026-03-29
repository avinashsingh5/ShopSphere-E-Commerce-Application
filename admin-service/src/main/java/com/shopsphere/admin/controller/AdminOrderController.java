package com.shopsphere.admin.controller;

import com.shopsphere.admin.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders(
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(adminOrderService.getAllOrders(username, role));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> statusRequest) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, statusRequest, username, role));
    }
}
