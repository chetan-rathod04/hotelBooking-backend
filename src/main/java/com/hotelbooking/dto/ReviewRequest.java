package com.hotelbooking.dto;


import lombok.Data;

@Data
public class ReviewRequest {
    private String hotelId;
    private String comment;
    private int rating;
    private String userId;
    private String username;
}
