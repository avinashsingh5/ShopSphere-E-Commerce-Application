package com.shopsphere.admin.controller;

import com.shopsphere.admin.dto.DashboardResponse;
import com.shopsphere.admin.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics(username, role));
    }
}
