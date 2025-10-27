package com.example.smartqr.controller;

import com.example.smartqr.dto.ApiResponse;
import com.example.smartqr.dto.auth.AuthResponse;
import com.example.smartqr.dto.auth.LoginRequest;
import com.example.smartqr.dto.auth.SignupRequest;
import com.example.smartqr.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            log.info("Signup request received for email: {}", request.getEmail());
            AuthResponse response = authService.signup(request);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
        } catch (RuntimeException e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request received for email: {}", request.getEmail());
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success("Token is valid"));
    }
}
