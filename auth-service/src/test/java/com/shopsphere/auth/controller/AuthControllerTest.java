package com.shopsphere.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.SignupRequest;
import com.shopsphere.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void signup_ShouldReturn201_WhenSuccessful() throws Exception {
        SignupRequest request = new SignupRequest("Test", "test@test.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("test@test.com").name("Test").role("CUSTOMER")
                .message("User registered successfully").build();

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void signup_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        SignupRequest request = new SignupRequest("Test", "test@test.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .message("Email already registered").build();

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void login_ShouldReturn200_WhenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("test@test.com").name("Test").role("CUSTOMER")
                .message("Login successful").build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword");
        AuthResponse response = AuthResponse.builder()
                .message("Invalid email or password").build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
