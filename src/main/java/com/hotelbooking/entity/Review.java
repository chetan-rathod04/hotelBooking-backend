package com.hotelbooking.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    private String id;
    private String hotelId;
    private String userId;
    private String username;
    private int rating; // 1 to 5
    private String comment;
    private LocalDateTime createdAt;
}
