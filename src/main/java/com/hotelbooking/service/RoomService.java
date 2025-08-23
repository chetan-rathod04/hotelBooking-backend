package com.hotelbooking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.RoomException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import com.mongodb.DuplicateKeyException;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository ;
    public Room addRoom(RoomRequest request) {
        validateRoomRequest(request);
        
        // ✅ Check for uniqueness of roomNumber
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new IllegalArgumentException("Room number already exists");
        }
        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setType(request.getType());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailable(request.isAvailable());
        room.setHotelId(request.getHotelId());

        try {
            return roomRepository.save(room);
        } catch (DuplicateKeyException e) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room number already exists (DB check)");
            throw new RuntimeException("Room number already exists. Please use a different number.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving room");
        }
    }
    public Room createRoom(Room room) {
    	 try {
    	        return roomRepository.save(room);
    	    } catch (DuplicateKeyException e) {
    	        throw new RuntimeException("Room with number '" + room.getRoomNumber() + "' already exists.");
    	    }    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotelId(String hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public Room getRoomById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RoomException("Room ID cannot be null or blank");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException("Room with ID '" + id + "' not found."));
    }

    public Room updateRoom(String id, RoomRequest request) throws RoomException {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            throw new RoomException("Room not found with id: " + id);
        }

        Room room = roomOpt.get();
        room.setRoomNumber(request.getRoomNumber());
        room.setType(request.getType());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailable(request.isAvailable());
        room.setImage(request.getImage());
        room.setHotelId(request.getHotelId());

        return roomRepository.save(room);
    }


    public void deleteRoom(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RoomException("Room ID cannot be null or blank");
        }

        if (!roomRepository.existsById(id)) {
            throw new RoomException("Room with ID '" + id + "' does not exist.");
        }

        roomRepository.deleteById(id);
    }

    // ✅ Validate room request data
    private void validateRoomRequest(RoomRequest request) {
        if (request == null) {
            throw new RoomException("Room request cannot be null.");
        }

        if (!StringUtils.hasText(request.getType())) {
            throw new RoomException("Room type is required.");
        }

        if (!StringUtils.hasText(request.getRoomNumber())) {
            throw new RoomException("Room number is required.");
        }

        if (request.getPricePerNight() < 0) {
            throw new RoomException("Room price must be a positive value.");
        }

        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new RoomException("Room number already exists.");
        }
        
  
    }
    public List<Room> findAvailableRooms(String hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> hotelRooms = roomRepository.findByHotelId(hotelId);
        List<Booking> overlappingBookings = bookingRepository.findByHotelIdAndDateRange(hotelId, checkIn, checkOut);

        Set<String> bookedRoomIds = overlappingBookings.stream()
                .map(Booking::getRoomId)
                .collect(Collectors.toSet());

        return hotelRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());
    }

}
