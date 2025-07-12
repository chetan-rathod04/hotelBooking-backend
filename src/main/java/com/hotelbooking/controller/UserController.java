package com.hotelbooking.controller;

import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import org.springframework.http.HttpHeaders;

import com.hotelbooking.dto.UserUpdateRequest;
import com.hotelbooking.entity.User;
import com.hotelbooking.security.JwtUtils;
import com.hotelbooking.service.UserService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils JwtUtils; 

//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateUser(
//            @PathVariable String id,
//            @RequestBody UserUpdateRequest request) {
//        try {
//            User updatedUser = userService.updateUser(id, request);
//            return ResponseEntity.ok(updatedUser);
//        } catch (RuntimeException ex) {
//            return ResponseEntity.badRequest().body(ex.getMessage());
//        }
//    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        try {
            User updatedUser = userService.updateUser(id, request);

            // 🔐 Generate new token using updated username
            String newToken = JwtUtils.generateToken(updatedUser.getUsername());

            // 🍪 Set token in HTTP-only cookie
            ResponseCookie cookie = ResponseCookie.from("token", newToken)
                    .httpOnly(true)
                    .secure(false) // set true in prod with HTTPS
                    .path("/")
                    .maxAge(600) // 10 minutes = same as JWT expiration
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(updatedUser);

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
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
}
