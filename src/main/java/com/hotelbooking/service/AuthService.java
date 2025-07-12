package com.hotelbooking.service;

import com.hotelbooking.dto.JwtResponse;
import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.RegisterRequest;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // ✅ User registration with email + username uniqueness check
    public String register(RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());

        userRepo.save(user);
        return "User registered successfully";
    }

    // ✅ User login with strong exception handling
    public JwtResponse login(LoginRequest req) {
        User user;
        try {
            user = userRepo.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + req.getUsername()));
        } catch (Exception e) {
            throw new RuntimeException("Login failed due to an internal error: " + e.getMessage());
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password for user: " + req.getUsername());
        }

        // Generate JWT token
        String token = jwtUtils.generateToken(user.getUsername());

        // You can add roles or ID in JwtResponse if needed
        return new JwtResponse(token, "Bearer", user.getUsername(), user.getRole());
    }
}
