package com.pharmacy.pharmacy_backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class TestBcryptController {

    private final BCryptPasswordEncoder passwordEncoder;

    public TestBcryptController(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/test-bcrypt")
    public String testBcrypt() {
        String rawPassword = "admin123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        return "Raw: " + rawPassword + "<br>" +
                "Hashed: " + hashedPassword + "<br>" +
                "Matches: " + passwordEncoder.matches(rawPassword, hashedPassword);
    }
}