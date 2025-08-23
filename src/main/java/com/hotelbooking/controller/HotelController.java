package com.hotelbooking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.dto.HotelRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.service.HotelService;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.HotelRepository;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "http://localhost:5173") // React frontend allow
public class HotelController {

    @Autowired
    private HotelService hotelService;
    @Autowired
    private HotelRepository hotelRepo;
    
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

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
    public ResponseEntity<?> getHotelById(@PathVariable String id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("Hotel not found with id: " + id);
            }
            return ResponseEntity.ok(hotel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid hotel ID format: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Server error: " + e.getMessage());
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Hotel> getHotelById(@PathVariable String id) {
//        Hotel hotel = hotelService.getHotelById(id);
//        if (hotel == null) {
//            throw new ResourceNotFoundException("Hotel with ID " + id + " not found.");
//        }
//        return ResponseEntity.ok(hotel);
//    }

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
    
    @GetMapping("/limited")
    public List<Hotel> getLimitedHotels(@RequestParam(defaultValue = "5") int count) {
        return hotelRepo.findLimitedHotels(PageRequest.of(0, count));
    }
    
 // ✅ Search hotels by name (for HeroSection search bar)
    @GetMapping("/search")
    public ResponseEntity<List<Hotel>> searchHotelsByName(@RequestParam("query") String query) {
        List<Hotel> hotels = hotelRepo.findByNameContainingIgnoreCase(query);
        return ResponseEntity.ok(hotels);
    }

    
    
}
