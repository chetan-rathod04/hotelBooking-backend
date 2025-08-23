package com.hotelbooking.dto;

import java.util.ArrayList;
import java.util.List;

import com.hotelbooking.entity.Review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotelRequest {
	@NotBlank(message = "name must not be blank")
    private String name	;
    @NotBlank(message = "location must not be blank")
    private String location;
	@NotBlank(message = "pricePerNight must not be blank")
    private double pricePerNight;
	@NotBlank(message = "available must not be blank")
    private boolean available;
	@NotBlank(message = "Hotel Number must not be blank")
	private String hotelNumber;
	private String image;
    private List<Review> reviews = new ArrayList<>();


}
