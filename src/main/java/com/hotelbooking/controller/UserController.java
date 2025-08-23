package com.hotelbooking.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.hotelbooking.dto.AdminUpdateUserRequest;
import com.hotelbooking.dto.RegisterRequest;
import com.hotelbooking.dto.UserUpdateRequest;
import com.hotelbooking.entity.User;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtUtils;
import com.hotelbooking.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController  {

	@Autowired
    private  UserService userService;
	@Autowired
    private final JwtUtils JwtUtils; 
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addUserByAdmin(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User added successfully"));
    }

    
    @PutMapping("/admin/update/{userId}")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable String userId,
            @RequestBody AdminUpdateUserRequest dto) {
        try {
            User updatedUser = userService.updateUserByAdmin(userId, dto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            // ❌ This sends plain text, not JSON
            // return ResponseEntity.badRequest().body(e.getMessage());

            // ✅ Return error object instead
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUserWithAvatar(
            @PathVariable String id,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        try {
            // ✅ Create DTO object from params
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername(username);
            request.setEmail(email);

            // ✅ Update the user and save avatar
            User updatedUser = userService.updateUser(id, request, avatar);

            // 🔐 Generate new JWT token after update (if needed)
            String newToken = JwtUtils.generateToken(updatedUser.getUsername());

            // 🍪 Set new token in HttpOnly cookie
            ResponseCookie cookie = ResponseCookie.from("token", newToken)
                    .httpOnly(true)
                    .secure(false)  // ❗ Set to true in production (HTTPS)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 days
                    .build();

            // ✅ Return updated user object + new token cookie
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(updatedUser);

        } catch (Exception ex) {
            ex.printStackTrace(); // For debugging (remove in prod)
            return ResponseEntity.status(500).body("Profile update failed: " + ex.getMessage());
        }
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    
    // 🔼 Upload avatar
    @PostMapping("/user/avatar/{userId}")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.uploadAvatar(username, file);
            return ResponseEntity.ok("Avatar uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading avatar: " + e.getMessage());
        }
    }

    // 🔽 Get avatar
    @GetMapping("/avatar")
    public ResponseEntity<String> getAvatar(Authentication authentication) {
        String username = authentication.getName();
        String base64Avatar = userService.getAvatar(username);
        return ResponseEntity.ok(base64Avatar);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName(); // gets from JWT
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Optional: sanitize response or convert to DTO
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ log the real error in backend console
            return ResponseEntity.status(500).body("Failed to fetch user profile: " + e.getMessage());
        }
    }

}
