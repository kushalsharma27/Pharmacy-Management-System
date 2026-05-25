package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.AuthResponse;
import com.pharmacy.pharmacy_backend.dto.LoginRequest;
import com.pharmacy.pharmacy_backend.dto.RegisterRequest;
import com.pharmacy.pharmacy_backend.model.User;
import com.pharmacy.pharmacy_backend.model.Role;
import com.pharmacy.pharmacy_backend.repository.UserRepository;
import com.pharmacy.pharmacy_backend.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = JwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
    }

    public AuthResponse register(RegisterRequest request) {
        // 🚫 BLOCK ADMIN REGISTRATION - Only one admin exists in DB
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("Admin account already exists. Cannot create additional admin.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        try {
            String roleStr = request.getRole().toUpperCase();
            Role role = Role.valueOf(roleStr);

            // 🚫 Double-check: Only allow PHARMACIST or CASHIER
            if (role == Role.ADMIN) {
                throw new RuntimeException("Admin account already exists. Cannot create additional admin.");
            }

            user.setRole(role);
        } catch (IllegalArgumentException e) {
            user.setRole(Role.CASHIER); // Default to CASHIER
        }

        user.setActive(true);
        userRepository.save(user);

        // Generate token for the new user
        String token = JwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
    }
}