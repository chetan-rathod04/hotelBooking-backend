package com.hotelbooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

	@NotBlank(message = "username must not be blank")
	private String username;
	@NotBlank(message = "Password must not be blank")
	private String password;
}
