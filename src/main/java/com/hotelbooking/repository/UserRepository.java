package com.hotelbooking.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hotelbooking.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	
    Optional<User> findByUsername(String username);
    Optional<User>findByEmail(String email);
}