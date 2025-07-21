package com.hotelbooking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingRequest {
	
	@NotBlank(message = "roomId must not be blank")
    private String roomId;
	@NotBlank(message = "userId must not be blank")
    private String userId;
//	@NotBlank(message = "nusername must not be blank")
//	private String username; 
	@NotBlank(message = "fromDate must not be blank")
    private LocalDate fromDate;
	@NotBlank(message = "toDate must not be blank")
    private LocalDate toDate;
//	@NotBlank(message = "Room Number must not be blank")
//    private String roomNumber;
//	@NotBlank(message = "Hotel Name must not be blank")
//    private String hotelName;

}
