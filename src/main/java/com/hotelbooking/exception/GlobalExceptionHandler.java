package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongodb.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Common error body builder
    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String error, String message) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", error);
        errorBody.put("message", message);
        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Object> handleBookingException(BookingException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Booking Error", ex.getMessage());
    }

    @ExceptionHandler(RoomException.class)
    public ResponseEntity<Object> handleRoomException(RoomException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, "Room Error", ex.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Object> handleConflict(ResourceConflictException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, "Conflict Error", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Runtime Error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpired(TokenExpiredException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, "Token Expired", ex.getMessage());
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        String message = "Duplicate value entered for a field that must be unique.";
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }


}
