package com.hotelbooking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hotelbooking.dto.HotelRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.exception.HotelException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.HotelRepository;
import org.springframework.util.StringUtils;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    // ✅ Create new hotel
    public Hotel addHotel(HotelRequest request) {
    	   if (!StringUtils.hasText(request.getHotelNumber())) {
    	        throw new HotelException("Hotel number is required.");
    	    }

    	    if (hotelRepository.findByHotelNumber(request.getHotelNumber()).isPresent()) {
    	        throw new HotelException("Hotel number already exists.");
    	    }
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setPricePerNight(request.getPricePerNight());
        hotel.setAvailable(request.isAvailable());
        hotel.setHotelNumber(request.getHotelNumber());
        return hotelRepository.save(hotel);
    }
    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel); // Will throw DuplicateKeyException if hotelNumber is not unique
    }

    // ✅ Get all hotels
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    // ✅ Get single hotel by ID
    public Hotel getHotelById(String id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));
    }

    // ✅ Search hotels by location
    public List<Hotel> getHotelsByLocation(String location) {
        List<Hotel> hotels = hotelRepository.findByLocationContainingIgnoreCase(location);
        if (hotels.isEmpty()) {
            throw new HotelException("No hotels found in location: " + location);
        }
        return hotels;
    }

    // ✅ Update hotel
    public Hotel updateHotel(String id, HotelRequest request) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + id);
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));

        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setPricePerNight(request.getPricePerNight());
        hotel.setAvailable(request.isAvailable());

        return hotelRepository.save(hotel);
    }

    // ✅ Delete hotel
    public void deleteHotel(String id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
