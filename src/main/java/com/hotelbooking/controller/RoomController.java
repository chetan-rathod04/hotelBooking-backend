package com.hotelbooking.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hotelbooking.exception.RoomException;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.service.RoomService;
import com.mongodb.DuplicateKeyException;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

public class RoomController {

    @Autowired
    private RoomService roomService;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private RoomRepository roomRepository;
    
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoom(@RequestBody Room room) {
        try {
            // ✅ Check if hotel exists
            Optional<Hotel> hotelOpt = hotelRepository.findById(room.getHotelId());
            if (hotelOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hotel not found with ID: " + room.getHotelId());
            }

            // ✅ Set default image if not provided
            if (room.getImage() == null || room.getImage().isEmpty()) {
                room.setImage("default-room.jpg");
            }

            // ✅ Save the room
            Room savedRoom = roomRepository.save(room);

            // ✅ Add room to hotel's room list
            Hotel hotel = hotelOpt.get();
            hotel.getRoomIds().add(savedRoom.getId());
            hotelRepository.save(hotel);

            return ResponseEntity.ok(savedRoom);
        }
        catch (DuplicateKeyException e) {
            return ResponseEntity.status(400).body("Room number already exists!");
//            throw new RuntimeException("Room number already exists. Please use a different number.");

        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving room: " + e.getMessage());
        }
        
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Failed to add room: " + e.getMessage());
//        }
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve rooms.");
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms() {
        try {
            List<Room> availableRooms = roomService.getAvailableRooms();
            return ResponseEntity.ok(availableRooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve available rooms.");
        }
    }

    @GetMapping("/getroom/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable String id) {
        try {
            Room room = roomService.getRoomById(id);
            return ResponseEntity.ok(room);
        } catch (RoomException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching room.");
        }
    }

    // ✅ Update room
    @PutMapping("/roomUpdate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoom(@PathVariable String id, @RequestBody RoomRequest request) {
        try {
            if (request.getImage() == null || request.getImage().isEmpty()) {
                request.setImage("default-room.jpg");
            }

            Room updatedRoom = roomService.updateRoom(id, request);
            return ResponseEntity.ok(updatedRoom);
        } catch (RoomException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e) {
            e.printStackTrace(); // 🔍 Log the exception stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to update room: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.noContent().build();
        } catch (RoomException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete room.");
        }
    }
    
    // Optional: Get rooms by hotelId
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getRoomsByHotel(@PathVariable String hotelId) {
        List<Room> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/limited")
    public List<Room> getLimitedRooms(@RequestParam(defaultValue = "5") int count) {
        return roomRepository.findLimitedRooms(PageRequest.of(0, count));
    }
    
    @GetMapping("/available/search")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam String hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        if (checkIn == null || checkOut == null) {
            return ResponseEntity.badRequest().body("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            return ResponseEntity.badRequest().body("Check-out date must be after check-in date.");
        }

        List<Room> availableRooms = roomService.findAvailableRooms(hotelId, checkIn, checkOut);

        return ResponseEntity.ok(availableRooms);
    }



}
