package com.hotelbooking.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hotelbooking.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Id
	private String id;
	@Indexed(unique = true)
	private String username;
	@Indexed(unique = true)
	private String email;
	private String password;
	private UserRole role; // USER / ADMIN
    private String avatar; // could be a URL or base64 string

}
