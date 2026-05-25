package com.pharmacy.pharmacy_backend.controller;

import com.pharmacy.pharmacy_backend.dto.AuthResponse;
import com.pharmacy.pharmacy_backend.dto.LoginRequest;
import com.pharmacy.pharmacy_backend.dto.RegisterRequest;
import com.pharmacy.pharmacy_backend.dto.ErrorResponse;
import com.pharmacy.pharmacy_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/hash")
    public String hashPassword(@RequestParam String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("========== LOGIN ATTEMPT ==========");
        System.out.println("Username received: '" + request.getUsername() + "'");
        System.out.println("Password length: " + (request.getPassword() != null ? request.getPassword().length() : 0));
        System.out.println("===================================");

        try {
            AuthResponse response = authService.login(request);
            System.out.println("✅ Login successful for user: " + response.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}