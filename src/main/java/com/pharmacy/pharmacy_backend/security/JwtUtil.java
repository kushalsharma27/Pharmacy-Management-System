package com.pharmacy.pharmacy_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final Key key = Keys.hmacShaKeyFor(
            "mysecretkeymysecretkeymysecretkey123".getBytes()
    );

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private JwtUtil() {}

    public static String generateToken(String username, String role) {
        String springSecurityRole = role;
        if (role != null && !role.startsWith("ROLE_")) {
            springSecurityRole = "ROLE_" + role;
        }

        return Jwts.builder()
                .setSubject(username)
                .claim("role", springSecurityRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public static Key getKey() {
        return key;
    }
}