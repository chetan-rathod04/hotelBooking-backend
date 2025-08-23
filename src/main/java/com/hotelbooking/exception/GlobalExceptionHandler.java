package com.hotelbooking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.mongodb.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	 // ✅ Common error response builder
    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String errorTitle, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", errorTitle);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
    

    // ✅ Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (var error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", "Validation failed for one or more fields.");
        body.put("fieldErrors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ✅ BookingException
    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Object> handleBookingException(BookingException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Booking Error", ex.getMessage());
    }
    
    // ✅ RoomException
    @ExceptionHandler(RoomException.class)
    public ResponseEntity<Object> handleRoomException(RoomException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, "Room Error", ex.getMessage());
    }

    // ✅ Resource Conflict
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Object> handleConflict(ResourceConflictException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, "Conflict Error", ex.getMessage());
    }

    // ✅ Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Runtime Error", ex.getMessage());
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleGeneric(Exception ex) {
//        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
//    }
    
    
    // ✅ Expired Token
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpired(TokenExpiredException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, "Token Expired", ex.getMessage());
    }
    
    // ✅ Duplicate Key Exception (like unique field: roomNumber)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateKeyException(DuplicateKeyException ex) {
        Map<String, String> error = new HashMap<>();
        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("hotelNumber")) {
                error.put("message", "Hotel number already exists!");
            } else if (message.contains("roomNumber")) {
                error.put("message", "Room number already exists!");
            } else {
                error.put("message", "Duplicate key error occurred.");
            }
        } else {
            error.put("message", "Duplicate key error occurred.");
        }

        error.put("error", "DuplicateKeyException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (ex.getRootCause() != null && ex.getRootCause().getMessage().contains("duplicate key")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room already exists with same room number!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Data integrity violation", ex.getMessage());
    }

    
    // ✅ Fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        if (ex.getMessage().contains("E11000 duplicate key error")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room with the same number already exists!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }


}
