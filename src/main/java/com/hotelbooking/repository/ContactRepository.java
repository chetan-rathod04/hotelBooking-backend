package com.hotelbooking.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hotelbooking.entity.ContactMessage;

public interface ContactRepository extends MongoRepository<ContactMessage, String> {

}
