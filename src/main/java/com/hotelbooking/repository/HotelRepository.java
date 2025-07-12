package com.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.hotelbooking.entity.Hotel;

public interface HotelRepository extends MongoRepository<Hotel, String> {
    List<Hotel> findByLocationContainingIgnoreCase(String location); // for partial match
    Optional<Hotel> findByHotelNumber(String hotelNumber);

}
