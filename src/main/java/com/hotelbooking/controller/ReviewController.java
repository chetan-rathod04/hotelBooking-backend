package com.hotelbooking.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.dto.ReviewRequest;
import com.hotelbooking.entity.Review;
import com.hotelbooking.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<Review> addReview(@RequestBody ReviewRequest request) {
        Review saved = reviewService.addReview(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable String hotelId) {
        return ResponseEntity.ok(reviewService.getReviewsForHotel(hotelId));
    }

    @GetMapping("/{hotelId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable String hotelId) {
        return ResponseEntity.ok(reviewService.getAverageRating(hotelId));
    }
}
