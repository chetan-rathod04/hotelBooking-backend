package com.hotelbooking.enums;

/**
 * Enum representing user roles.
 */
public enum UserRole {
    USER,
    ADMIN;

    // Optional: Custom toString() to always return role in uppercase
    @Override
    public String toString() {
        return name().toUpperCase();
    }
}
