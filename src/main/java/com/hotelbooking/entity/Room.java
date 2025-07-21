package com.hotelbooking.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id private String id;
    @Indexed(unique = true)
    private String roomNumber;
    private String hotelId;
    private String type; // Single, Double, Suite
    private double pricePerNight;
    private boolean available;
    private String image;
}
