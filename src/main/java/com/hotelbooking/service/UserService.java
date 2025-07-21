package com.hotelbooking.service;

import com.hotelbooking.dto.UserUpdateRequest;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.UserRole;
import com.hotelbooking.exception.ResourceConflictException;
import com.hotelbooking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
    private final UserRepository userRepository;
    private final String uploadDir = "uploads/avatars";

    
    public User updateUser(String id, UserUpdateRequest request, MultipartFile avatar) throws IOException {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with ID: " + id);
        }

        User user = userOptional.get();

        // ✅ Check for unique username
        if (StringUtils.hasText(request.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new ResourceConflictException("Username already exists: " + request.getUsername());
                }
            });
            user.setUsername(request.getUsername());
        }

        // ✅ Check for unique email
        if (StringUtils.hasText(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new ResourceConflictException("Email already exists: " + request.getEmail());
                }
            });
            user.setEmail(request.getEmail());
        }

        // ✅ Handle avatar upload
        if (avatar != null && !avatar.isEmpty()) {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = UUID.randomUUID().toString() + "_" + avatar.getOriginalFilename();
            File dest = new File(dir, filename);
            avatar.transferTo(dest);

            user.setAvatar("/uploads/avatars/" + filename); // Only store relative path
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
