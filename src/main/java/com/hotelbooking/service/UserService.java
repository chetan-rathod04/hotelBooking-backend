package com.hotelbooking.service;

import com.hotelbooking.dto.UserUpdateRequest;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.UserRole;
import com.hotelbooking.exception.ResourceConflictException;
import com.hotelbooking.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // ✅ Unique Username Check
        if (StringUtils.hasText(request.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new ResourceConflictException("Username already exists: " + request.getUsername());
                }
            });
            user.setUsername(request.getUsername());
        }

        // ✅ Unique Email Check
        if (StringUtils.hasText(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new ResourceConflictException("Email already exists: " + request.getEmail());
                }
            });
            user.setEmail(request.getEmail());
        }

        // ✅ Password update
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // ✅ Role update
        if (StringUtils.hasText(request.getRole())) {
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role. Allowed values: USER, ADMIN");
            }
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
//    Avatar code
    
    public User uploadAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Convert image to Base64
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        user.setAvatar(base64Image);

        return userRepository.save(user);
    }

    public String getAvatar(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAvatar();
    }
}
