package com.shopsphere.auth.service;

import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.SignupRequest;
import com.shopsphere.auth.model.Role;
import com.shopsphere.auth.model.User;
import com.shopsphere.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest("Test User", "test@test.com", "password123");
        loginRequest = new LoginRequest("test@test.com", "password123");
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void signup_ShouldReturnToken_WhenEmailNotRegistered() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_ShouldReturnError_WhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        AuthResponse response = authService.signup(signupRequest);

        assertNull(response.getToken());
        assertEquals("Email already registered", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        AuthResponse response = authService.login(loginRequest);

        assertNull(response.getToken());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void login_ShouldReturnError_WhenPasswordInvalid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        AuthResponse response = authService.login(loginRequest);

        assertNull(response.getToken());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void signup_ShouldAssignAdminRole_WhenRoleIsAdmin() {
        SignupRequest adminRequest = new SignupRequest("Admin", "admin@test.com", "password123");
        User adminUser = User.builder()
                .id(2L).name("Admin").email("admin@test.com")
                .password("encoded").role(Role.ADMIN).build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("admin-token");

        AuthResponse response = authService.signup(adminRequest);

        assertEquals("ADMIN", response.getRole());
    }
}
