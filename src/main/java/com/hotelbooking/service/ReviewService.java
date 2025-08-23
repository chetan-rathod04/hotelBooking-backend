package com.hotelbooking.service;

import com.hotelbooking.dto.ReviewRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Review;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;

    public Review addReview(ReviewRequest request) {
        Review review = Review.builder()
                .hotelId(request.getHotelId())
                .userId(request.getUserId())
                .username(request.getUsername())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // ⭐ Update average rating for the hotel
        List<Review> allReviews = reviewRepository.findByHotelId(request.getHotelId());
        double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        // Fetch hotel and update average rating
        Optional<Hotel> optionalHotel = hotelRepository.findById(request.getHotelId());
        optionalHotel.ifPresent(hotel -> {
            hotel.setAverageRating(avg);
            hotelRepository.save(hotel);
        });

        return savedReview;
    }

    public List<Review> getReviewsForHotel(String hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }

    public double getAverageRating(String hotelId) {
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
