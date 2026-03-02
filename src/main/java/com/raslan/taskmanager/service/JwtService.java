package com.raslan.taskmanager.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public String generateToken(String email, Long userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }




    public boolean isTokenValid(String token) {
        try{
        Claims claims = extractAllClaims(token);
        boolean isTokenExpired = claims.getExpiration().before(new Date());

        return !isTokenExpired;
        }
        catch (JwtException e){
            return false;
        }
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean isTokenValidForUser(String token, UserDetails userDetails) {
        final String email = extractEmail(token);

        return Objects.equals(email, userDetails.getUsername());
    }


    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Key getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try{
            Claims claims = extractAllClaims(token);
            String email = claims.getSubject();
            boolean expired = claims.getExpiration().before(new Date());

            return !expired && Objects.equals(email, userDetails.getUsername());
        }

        catch (JwtException e){
            return false;
        }
    }

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
    }
}
