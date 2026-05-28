package com.pharmacy.pharmacy_backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // ✅ CORS Configuration Bean - Define this FIRST
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://project-6thej.vercel.app", "https://project-6thej-6qfi1kmca-kushal-sharma-projects2.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 Configuring SecurityFilterChain...");

        http
                .csrf(csrf -> csrf.disable())
                // ✅ Now we can reference the corsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 🔓 PUBLIC ENDPOINTS
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/hash",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()

                        // 👥 SUPPLIER ENDPOINTS - READ
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")

                        // 👥 SUPPLIER ENDPOINTS - WRITE
                        .requestMatchers(HttpMethod.POST, "/api/suppliers")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/suppliers/*/activate")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/suppliers/*/deactivate")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**")
                        .hasRole("ADMIN")

                        // 📦 PURCHASE ORDER ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/purchase-orders")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")

                        // 📊 INVENTORY ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/inventory/**")
                        .hasRole("ADMIN")

                        // 💊 MEDICINE ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/medicines/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/medicines")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, "/api/medicines/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/medicines/**")
                        .hasRole("ADMIN")

                        // 👥 CUSTOMER ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/customers/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/customers")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/customers/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**")
                        .hasRole("ADMIN")

                        // 🧾 SALES ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/sales/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/sales")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/sales/*/process-payment")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/sales/*/status")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/sales/*/cancel")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers("/api/sales/statistics/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")

                        // 👤 USER ENDPOINTS
                        .requestMatchers(HttpMethod.POST, "/api/users/change-password")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.GET, "/api/users/profile")
                        .hasAnyRole("ADMIN", "PHARMACIST", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/reset-password")
                        .hasRole("ADMIN")

                        // 🔒 ALL OTHER REQUESTS NEED AUTHENTICATION
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("✅ SecurityFilterChain configured successfully");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}