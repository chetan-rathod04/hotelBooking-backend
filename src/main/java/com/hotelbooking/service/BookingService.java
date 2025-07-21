package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.exception.BookingException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private RoomRepository roomRepository;
    
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
            throw new BookingException("Cannot book past dates. Booking must start from today or a future date.");
        }

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new BookingException("'fromDate' must be before or equal to 'toDate'.");
        }

        // ✅ Fetch Room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BookingException("Room not found"));

        if (!room.isAvailable()) {
            throw new BookingException("Room is not available");
        }

        // ✅ Fetch Hotel
        Hotel hotel = hotelRepository.findById(room.getHotelId())
                .orElseThrow(() -> new BookingException("Hotel not found"));

        // ✅ Check booking overlap
        List<Booking> overlappingBookings = bookingRepository
                .findByRoomIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        room.getId(), request.getToDate(), request.getFromDate());

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Room is already booked for the selected date range.");
        }

        // ✅ Generate unique booking number
        String bookingNumber = "BK-" + UUID.randomUUID().toString().substring(0, 3).toUpperCase();

        // Optional: Ensure uniqueness (very rare case)
        if (bookingRepository.findByBookingNumber(bookingNumber).isPresent()) {
            throw new BookingException("Duplicate booking number generated. Please try again.");
        }

        // ✅ Fetch User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BookingException("User not found"));

        // ✅ Create and save Booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setBookingNumber(bookingNumber);
        booking.setUserId(user.getId());
        booking.setUsername(user.getUsername());
        booking.setRoomId(room.getId());
        booking.setRoomNumber(room.getRoomNumber());
        booking.setHotelName(hotel.getName());
        booking.setFromDate(request.getFromDate());
        booking.setToDate(request.getToDate());
        booking.setPricePerNight(room.getPricePerNight());
        booking.setStatus(BookingStatus.PENDING); // Always start with PENDING

        return bookingRepository.save(booking);
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

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Hotel Booking Invoice", font));
            document.add(new Paragraph("-----------------------------------------------------", small));
            document.add(new Paragraph("Booking Number: " + booking.getBookingNumber(), small));
            document.add(new Paragraph("User: " + booking.getUsername(), small));
            document.add(new Paragraph("Hotel: " + booking.getHotelName(), small));
            document.add(new Paragraph("Room: " + booking.getRoomNumber(), small));
            document.add(new Paragraph("From: " + booking.getFromDate(), small));
            document.add(new Paragraph("To: " + booking.getToDate(), small));
            document.add(new Paragraph("Status: " + booking.getStatus(), small));
            document.add(new Paragraph("Price per night: ₹" + booking.getPricePerNight(), small));

            long nights = booking.getToDate().toEpochDay() - booking.getFromDate().toEpochDay() + 1;
            double total = nights * booking.getPricePerNight();

            document.add(new Paragraph("Nights: " + nights, small));
            document.add(new Paragraph("Total: ₹" + total, small));

            document.add(new Paragraph("\nThank you for booking with us.", small));
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + booking.getBookingNumber() + ".pdf");

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
