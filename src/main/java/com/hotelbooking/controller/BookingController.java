package com.hotelbooking.controller;

import java.util.List;

import com.hotelbooking.exception.BookingException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.service.BookingService;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;

    // ✅ Utility method for reuse
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }
    
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> bookRoom(@RequestBody BookingRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BookingException("User not found"));

            request.setUserId(user.getId()); // Inject userId from token

            Booking booking = bookingService.createBooking(request);
            return ResponseEntity.ok(booking);

        } catch (BookingException ex) {
            return ResponseEntity.badRequest().body("Booking Error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Server Error: " + ex.getMessage());
        }
    }



    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserBookings(@PathVariable String userId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByUser(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("Error fetching user bookings: " + ex.getMessage());
        }
    }


    @GetMapping("/allbooking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception ex) {
            ex.printStackTrace(); // Add this for debug
            return ResponseEntity.internalServerError().body("Failed to retrieve bookings");
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> cancelBooking(@PathVariable String id, Authentication authentication) {
        try {
            Booking booking = bookingService.getBookingById(id);
            String loggedInUsername = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

            // ✅ Only allow user to delete their own booking
            if (!isAdmin && !booking.getUsername().equals(loggedInUsername)) {
                return ResponseEntity.status(403).body("You are not allowed to delete this booking.");
            }

            bookingService.deleteBooking(id);
            return ResponseEntity.ok("Booking cancelled successfully.");

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error deleting booking: " + ex.getMessage());
        }
    }
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingsByStatus(@PathVariable String status) {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            List<Booking> filtered = bookings.stream()
                .filter(b -> b.getStatus().name().equalsIgnoreCase(status))
                .toList();
            return ResponseEntity.ok(filtered);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Error filtering bookings");
        }
    }
 
//cancelstatus code
    @PutMapping("/cancelstatus/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> updateCancelStatus(@PathVariable String id, Authentication authentication) {
        try {
            Booking booking = bookingService.getBookingById(id);
            String loggedInUsername = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

            // ✅ Only user who made the booking or admin can cancel
            if (!isAdmin && !booking.getUsername().equals(loggedInUsername)) {
                return ResponseEntity.status(403).body("You are not allowed to cancel this booking.");
            }

            booking.setStatus(BookingStatus.CANCELLED);
            bookingService.save(booking);

            return ResponseEntity.ok("Booking status updated to CANCELLED.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to cancel booking: " + e.getMessage());
        }
    }

//    Add Endpoint to Download Invoice
    @GetMapping("/invoice/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String id, Authentication authentication) {
        try {
            Booking booking = bookingService.getBookingById(id);
            String loggedInUsername = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !booking.getUsername().equals(loggedInUsername)) {
                return ResponseEntity.status(403).build();
            }

            return bookingService.generateInvoicePdf(booking);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
