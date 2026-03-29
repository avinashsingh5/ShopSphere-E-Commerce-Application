package com.shopsphere.auth.controller;

import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.CreateAdminRequest;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.SignupRequest;
import com.shopsphere.auth.service.AuthService;
import com.shopsphere.auth.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/create-admin")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponse> createAdmin(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateAdminRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().message("Missing or invalid Authorization header").build());
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().message("Invalid token").build());
        }

        String role = jwtService.extractRole(token);
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.builder().message("Access denied").build());
        }

        AuthResponse response = authService.createAdmin(request);
        if (response.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}