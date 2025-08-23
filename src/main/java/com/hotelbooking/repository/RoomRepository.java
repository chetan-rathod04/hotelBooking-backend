package com.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.hotelbooking.entity.Room;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByAvailableTrue();
    List<Room> findByType(String type);
    List<Room> findByHotelId(String hotelId);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByHotelIdAndAvailableTrue(String hotelId);
    List<Room> findByPricePerNightBetween(double min, double max);
    List<Room> findByHotelIdAndType(String hotelId, String type);
    boolean existsByRoomNumber(String roomNumber);

    @Query("{}")
    List<Room> findLimitedRooms(Pageable pageable);
    
}
