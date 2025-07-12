package com.hotelbooking.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hotelbooking.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Document(collection = "Bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id 
    private String id;
    private String userId;
    private String roomId;
    
    @Indexed(unique = true)
    private String bookingNumber;
    
    private LocalDate fromDate;
    private LocalDate toDate;
    private String username;
//    private LocalDate bookingDate;
    private String roomNumber;  
    private String hotelName;    
    private BookingStatus status;  //pending, running, completed  
    private double pricePerNight;

}
