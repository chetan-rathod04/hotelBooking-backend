package com.hotelbooking.dto;

import com.hotelbooking.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RegisterRequest {
	@NotBlank(message = "username must not be blank")
	private String username;
    @Email(message = "Email should be valid")
	@NotBlank(message = "email must not be blank")
	private String email;
	@NotBlank(message = "password must not be blank")
//    @Size(min = 6, message = "Password must be at least 6 characters")
	@Pattern(
		      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
		      message = "Password must be at least 8 characters long, include 1 number, 1 letter, and 1 special symbol"
		    )
	private String password;
	@NotNull(message = "Role is required")
    private UserRole role;
}
