package com.shopsphere.auth.service;

import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.CreateAdminRequest;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.SignupRequest;
import com.shopsphere.auth.model.Role;
import com.shopsphere.auth.model.User;
import com.shopsphere.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .message("Email already registered")
                    .build();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .message("Invalid email or password")
                    .build();
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    public AuthResponse createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .message("Email already registered")
                    .build();
        }

        User admin = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        return AuthResponse.builder()
                .email(admin.getEmail())
                .name(admin.getName())
                .role(admin.getRole().name())
                .message("Admin created successfully")
                .build();
    }
}