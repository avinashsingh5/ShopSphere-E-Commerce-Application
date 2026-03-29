package com.shopsphere.admin.controller;

import com.shopsphere.admin.dto.DashboardResponse;
import com.shopsphere.admin.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ReportController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getReports(
            @RequestHeader("X-User-Email") String username,
            @RequestHeader("X-User-Role") String role) {
        // Reports currently uses the same dashboard metrics
        // Can be extended with more detailed analytics
        return ResponseEntity.ok(dashboardService.getDashboardMetrics(username, role));
    }
}
