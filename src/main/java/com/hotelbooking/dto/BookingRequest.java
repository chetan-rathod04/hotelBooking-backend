package com.hotelbooking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank(message = "roomId must not be blank")
    private String roomId;

//    @NotBlank(message = "userId must not be blank")
    private String userId;

//    @NotBlank(message = "hotelId must not be blank")
    private String hotelId;

    @NotNull(message = "fromDate must not be null")
    private LocalDate fromDate;

    @NotNull(message = "toDate must not be null")
    private LocalDate toDate;
}
