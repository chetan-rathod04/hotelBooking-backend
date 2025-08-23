package com.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hotelbooking.entity.Hotel;

public interface HotelRepository extends MongoRepository<Hotel, String> {
    List<Hotel> findByLocationContainingIgnoreCase(String location); // for partial match
    Optional<Hotel> findByHotelNumber(String hotelNumber);
//    @Query("{}")
//    List<Hotel> findLimitedHotels(Pageable pageable);

    // Case-insensitive hotel name search
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Hotel> findByNameContainingIgnoreCase(String name);
    // Existing method for limited hotels
    @Query("{}")
    List<Hotel> findLimitedHotels(org.springframework.data.domain.Pageable pageable);
    @Query("{ '$or': [ { 'name': { $regex: ?0, $options: 'i' } }, { 'location': { $regex: ?0, $options: 'i' } } ] }")
    List<Hotel> searchByNameOrLocation(String keyword);

}
