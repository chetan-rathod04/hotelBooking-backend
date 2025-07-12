package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.exception.BookingException;
import com.hotelbooking.repository.BookingRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.text.NumberFormat;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    // ✅ Main Method: Get bookings by user and update statuses
    public List<Booking> getBookingsByUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BookingException("User ID is required.");
        }

        List<Booking> bookings = bookingRepository.findByUserId(userId);
        updateStatuses(bookings);
        return bookings;
    }

    // ✅ Create booking with validations and default PENDING status
    public Booking createBooking(BookingRequest request) {
        LocalDate today = LocalDate.now();

        // 🛡️ Validate input
        if (request == null) {
            throw new BookingException("Booking request cannot be null.");
        }

        if (!StringUtils.hasText(request.getRoomId())) {
            throw new BookingException("Room ID is required.");
        }

        if (!StringUtils.hasText(request.getUserId())) {
            throw new BookingException("User ID is required.");
        }

        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new BookingException("Both 'fromDate' and 'toDate' must be provided.");
        }

        if (request.getFromDate().isBefore(today)) {
            throw new BookingException("Cannot book for past dates. Booking must start from today or a future date.");
        }

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new BookingException("'fromDate' must be before or equal to 'toDate'.");
        }

        // 🛡️ Overlapping check: Room cannot be double-booked
        List<Booking> overlappingBookings = bookingRepository
                .findByRoomIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        request.getRoomId(), request.getToDate(), request.getFromDate());

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Room is already booked for the selected date range.");
        }

        // ✅ Generate unique booking number
        String bookingNumber = generateBookingNumber();

        // Just to be safe (optional check)
        Optional<Booking> existing = bookingRepository.findByBookingNumber(bookingNumber);
        if (existing.isPresent()) {
            throw new BookingException("Duplicate booking number generated. Please try again.");
        }

        // ✅ Save Booking
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setUserId(request.getUserId());
        booking.setRoomId(request.getRoomId());
        booking.setUsername(request.getUsername());
        booking.setFromDate(request.getFromDate());
        booking.setToDate(request.getToDate());
        booking.setRoomNumber(request.getRoomNumber());
        booking.setHotelName(request.getHotelName());
        booking.setStatus(BookingStatus.PENDING); // Always set to PENDING at creation

        return bookingRepository.save(booking);
    }

    // ✅ Generate booking number like: BK-XYZ123
    private String generateBookingNumber() {
        String random = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "BK-" + random;
    }


    // ✅ Get all bookings, auto-update status
    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        updateStatuses(bookings);
        return bookings;
    }

    // ✅ Get one booking and auto-update status
    public Booking getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found with ID: " + id));

        updateStatus(booking);
        return booking;
    }

    // ✅ Delete booking by ID
    public void deleteBooking(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BookingException("Booking ID is required.");
        }

        if (!bookingRepository.existsById(id)) {
            throw new BookingException("Booking not found with ID: " + id);
        }

        bookingRepository.deleteById(id);
    }

    // ✅ Auto update multiple bookings
    private void updateStatuses(List<Booking> bookings) {
        bookings.forEach(this::updateStatus);
    }

    // ✅ Auto update status of a single booking
    private void updateStatus(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) return;

        LocalDate today = LocalDate.now();
        BookingStatus newStatus;

        if (today.isBefore(booking.getFromDate())) {
            newStatus = BookingStatus.PENDING;
        } else if (!today.isAfter(booking.getToDate())) {
            newStatus = BookingStatus.RUNNING;
        } else {
            newStatus = BookingStatus.COMPLETED;
        }

        if (!booking.getStatus().equals(newStatus)) {
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
        }
    }
    
    
 // ✅ Save or update booking
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
    // 🔽 PDF Invoice Generator Method (added in BookingService directly)
    public ResponseEntity<byte[]> generateInvoicePdf(Booking booking) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Add logo, title, and booking info as before...

            document.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "invoice_" + booking.getBookingNumber() + ".pdf");
            headers.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
