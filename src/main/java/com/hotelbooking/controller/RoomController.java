package com.hotelbooking.controller;

import java.util.List;

import com.hotelbooking.exception.RoomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoom(@RequestBody RoomRequest request) {
        try {
        	 // ✅ Set default image if not provided
            if (request.getImage() == null || request.getImage().isEmpty()) {
                request.setImage("default-room.jpg");
            }
            Room room = roomService.addRoom(request);
            return ResponseEntity.ok(room);
        } catch (RoomException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add room: " + e.getMessage());
        }
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update room.");
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
}
