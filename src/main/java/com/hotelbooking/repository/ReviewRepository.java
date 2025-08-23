package com.hotelbooking.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hotelbooking.entity.Review;

public interface ReviewRepository extends MongoRepository<Review, String>{
    List<Review> findByHotelId(String hotelId);

}
