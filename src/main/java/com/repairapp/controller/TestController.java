package com.repairapp.controller;

import com.repairapp.entity.User;
import com.repairapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create-exact-user")
    public ResponseEntity<Map<String, Object>> createExactTestUser() {
        try {
            // Delete existing admin user first
            userRepository.findByPhone("+91 7026666835").ifPresent(user -> userRepository.delete(user));
            
            // Create test user with exact phone format from JSON
            User testUser = new User();
            testUser.setName("Test Admin");
            testUser.setEmail("admin@test.com");
            testUser.setPassword(passwordEncoder.encode("admin123"));
            testUser.setPhone("+91 7026666835"); // Exact match with test JSON
            testUser.setRole(User.Role.ADMIN);
            testUser.setIsActive(true);
            testUser.setPhoneVerified(true);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(testUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exact test user created successfully");
            response.put("userId", savedUser.getId());
            response.put("phone", savedUser.getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create exact test user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        try {
            // Check if user already exists
            if (userRepository.findByPhone("+917026666835").isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Test user already exists");
                return ResponseEntity.ok(response);
            }

            // Create test user
            User testUser = new User();
            testUser.setName("Test Admin");
            testUser.setEmail("admin@test.com");
            testUser.setPassword(passwordEncoder.encode("admin123"));
            testUser.setPhone("+91 7026666835");
            testUser.setRole(User.Role.ADMIN);
            testUser.setIsActive(true);
            testUser.setPhoneVerified(true);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(testUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test user created successfully");
            response.put("userId", savedUser.getId());
            response.put("phone", savedUser.getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create test user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
