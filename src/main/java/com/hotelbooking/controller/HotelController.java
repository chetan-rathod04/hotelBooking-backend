package com.hotelbooking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.dto.HotelRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.service.HotelService;
import com.hotelbooking.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    // ✅ Create Hotel
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> addHotel(@RequestBody HotelRequest request) {
        try {
            return ResponseEntity.ok(hotelService.addHotel(request));
        } catch (Exception e) {
            throw new RuntimeException("Error while adding hotel: " + e.getMessage());
        }
    }

    @PostMapping("/add-multiple")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Hotel>> addMultipleHotels(@RequestBody List<HotelRequest> requests) {
        try {
            List<Hotel> addedHotels = requests.stream()
                .map(hotelService::addHotel)
                .toList();
            return ResponseEntity.ok(addedHotels);
        } catch (Exception e) {
            throw new RuntimeException("Error while adding multiple hotels: " + e.getMessage());
        }
    }

    // ✅ Get all hotels
    @GetMapping("/all")
    public ResponseEntity<List<Hotel>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    // ✅ Get hotel by ID
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String id) {
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel == null) {
            throw new ResourceNotFoundException("Hotel with ID " + id + " not found.");
        }
        return ResponseEntity.ok(hotel);
    }

    // ✅ Search by location
    @GetMapping("/location/search")
    public ResponseEntity<List<Hotel>> searchByLocation(@RequestParam String location) {
        List<Hotel> hotels = hotelService.getHotelsByLocation(location);
        if (hotels.isEmpty()) {
            throw new ResourceNotFoundException("No hotels found in location: " + location);
        }
        return ResponseEntity.ok(hotels);
    }

    // ✅ Update hotel
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> updateHotel(@PathVariable String id, @RequestBody HotelRequest request) {
        try {
            return ResponseEntity.ok(hotelService.updateHotel(id, request));
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("Cannot update. Hotel not found with ID: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Error updating hotel: " + e.getMessage());
        }
    }

    // ✅ Delete hotel
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHotel(@PathVariable String id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok("Hotel deleted successfully");
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("Cannot delete. Hotel not found with ID: " + id);
        }
    }
}
