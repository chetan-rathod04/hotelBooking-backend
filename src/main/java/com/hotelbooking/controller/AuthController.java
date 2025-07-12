package com.hotelbooking.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.RegisterRequest;
import com.hotelbooking.security.JwtUtils;
import com.hotelbooking.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            String jwtToken = jwtUtils.generateToken(username);

            ResponseCookie cookie = ResponseCookie.from("token", jwtToken)
                    .httpOnly(true)
                    .secure(false) // should be true in production
                    .path("/")
                    .maxAge(10 * 60)
                    .build();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("username", username);
            responseBody.put("role", role);

            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body(responseBody);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("Logged out");
    }

    @GetMapping("/check")
    public String check() {
        return "Application Running";
    }

    // ✅ USER ONLY: Get Booking History
    @GetMapping("/booking-history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBookingHistory(Authentication authentication) {
        String username = authentication.getName();
        // call bookingService.getBookingsByUsername(username)
        return ResponseEntity.ok("Booking history for user: " + username);
    }

    // ✅ ADMIN ONLY: Add Room
    @PostMapping("/admin/add-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoom(@RequestBody Map<String, Object> roomData) {
        // call roomService.save(roomData)
        return ResponseEntity.ok("Room added successfully");
    }

    // ✅ ADMIN ONLY: View all bookings
    @GetMapping("/admin/all-bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        // call bookingService.getAllBookings()
        return ResponseEntity.ok("All bookings for admin");
    }
}
