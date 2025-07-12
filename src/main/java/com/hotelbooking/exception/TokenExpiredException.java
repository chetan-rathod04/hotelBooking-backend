package com.hotelbooking.exception;
//🔹 package: com.hotelbooking.exception
public class TokenExpiredException extends RuntimeException {
 public TokenExpiredException(String message) {
     super(message);
 }
}

