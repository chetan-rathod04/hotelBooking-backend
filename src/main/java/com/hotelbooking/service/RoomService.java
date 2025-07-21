package com.hotelbooking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.RoomException;
import com.hotelbooking.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public Room addRoom(RoomRequest request) {
        validateRoomRequest(request);

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setType(request.getType());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailable(request.isAvailable());
        room.setHotelId(request.getHotelId());

        return roomRepository.save(room);
    }
    public Room createRoom(Room room) {
        return roomRepository.save(room); // roomNumber must be unique
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotelId(String hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public Room getRoomById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RoomException("Room ID cannot be null or blank");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException("Room with ID '" + id + "' not found."));
    }

    public Room updateRoom(String id, RoomRequest request) {
        if (!StringUtils.hasText(id)) {
            throw new RoomException("Room ID cannot be null or blank");
        }

        validateRoomRequest(request);

        Room existingRoom = getRoomById(id);
        existingRoom.setType(request.getType());
        existingRoom.setPricePerNight(request.getPricePerNight());
        existingRoom.setAvailable(request.isAvailable());

        return roomRepository.save(existingRoom);
    }

    public void deleteRoom(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RoomException("Room ID cannot be null or blank");
        }

        if (!roomRepository.existsById(id)) {
            throw new RoomException("Room with ID '" + id + "' does not exist.");
        }

        roomRepository.deleteById(id);
    }

    // ✅ Validate room request data
    private void validateRoomRequest(RoomRequest request) {
        if (request == null) {
            throw new RoomException("Room request cannot be null.");
        }

        if (!StringUtils.hasText(request.getType())) {
            throw new RoomException("Room type is required.");
        }

        if (!StringUtils.hasText(request.getRoomNumber())) {
            throw new RoomException("Room number is required.");
        }

        if (request.getPricePerNight() < 0) {
            throw new RoomException("Room price must be a positive value.");
        }

        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new RoomException("Room number already exists.");
        }
    }

}
