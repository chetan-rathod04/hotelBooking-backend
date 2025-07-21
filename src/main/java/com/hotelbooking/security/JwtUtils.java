package com.hotelbooking.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.hotelbooking.exception.TokenExpiredException;

@Component
public class JwtUtils {

    private final String secret = "G2yT30nB6gDm6HVs9mS9AwL0QbUcmD1T3mNEaBNKJDoRWE7Fdh4c3qCZg8V5EBLp";
    private final long expiration = 86400000; // 1 day

    private final Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    // ✅ Generate token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // ✅ Extract username
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // ✅ Extract role (if you put "role" in claim)
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ✅ Validate token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT");
        } catch (MalformedJwtException e) {
            System.out.println("Malformed JWT");
        } catch (SignatureException e) {
            System.out.println("Invalid signature");
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal argument token");
        }
        return false;
    }

    // ✅ Parse claims
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
 // 🔹 inside JwtUtils
    public boolean validateToken1(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired. Please login again.");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Unsupported JWT token.");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed JWT token.");
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid token signature.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty.");
        }
    }

}
