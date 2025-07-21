package com.hotelbooking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.enums.BookingStatus;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
	  List<Booking> findByRoomIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
		        String roomId, LocalDate toDate, LocalDate fromDate);
    List<Booking> findByUserId(String userId);
    Optional<Booking> findByBookingNumber(String bookingNumber);
    List<Booking> findByStatus(BookingStatus status);

 // BookingRepository.java
    @Query("{ 'roomId': ?0, 'status': { $ne: 'CANCELLED' }, $or: [ { 'fromDate': { $lte: ?2 }, 'toDate': { $gte: ?1 } } ] }")
    List<Booking> findByRoomIdAndDateOverlap(String roomId, LocalDate fromDate, LocalDate toDate);

}
