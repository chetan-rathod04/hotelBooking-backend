package com.hotelbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
	@NotBlank(message = "HoteId must not be blank")
	private String hotelId;
	@NotBlank(message = "RoomType must not be blank")
	private String type; // e.g., Deluxe, Single
	@Min(0)
	private double pricePerNight;
	private boolean available;
	@NotBlank(message = "Room Number must not be blank")
	private String roomNumber;
	private String image;
}
