package com.hotelbooking.dto;

import com.hotelbooking.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RegisterRequest {
	@NotBlank(message = "username must not be blank")
	private String username;
	@NotBlank(message = "email must not be blank")
	private String email;
	@NotBlank(message = "password must not be blank")
	private String password;
    private UserRole role;
}
