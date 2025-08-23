//package com.hotelbooking.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.hotelbooking.dto.AdminUpdateUserRequest;
//import com.hotelbooking.entity.User;
//
//@RestController
//@RequestMapping("/api/admin")
//@Validated
//public class AdminController {
//
//	@Autowired
//	public AdminUpdateUserRequest adminUpdateUserRequest;
//	@PutMapping("/users/{userId}")
//	@PreAuthorize("hasRole('ADMIN')")
//	public ResponseEntity<?> updateUserByAdmin(
//	        @PathVariable String userId,
//	        @RequestBody AdminUpdateUserRequest adminUpdateUserRequest) {
//
//	    User updatedUser = userService.updateUserByAdmin(userId, adminUpdateUserRequest);
//	    return ResponseEntity.ok(updatedUser);
//	}
//
//}
