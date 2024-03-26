package com.example.demo.security;

import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public static  String SECRET_KEY = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEA0eZekL5vIkbUiB4DeCLhiqC1mG5uOfLU/IPno3SfuLQmsIMGcLYQVjSl7QKKufeMxMhtLwe7dIj6S9yoYlX89QIDAQABAkEArjLYTJwfM31BKW5vRqb8M8bIrycHB8TGioMWHQvO+sPXh4e0vusaIj3VJgIwr/U65G3riifsetENSm5qa7YHyQIhAOof9n64K9zlg6mek/GBm386zlcHGeJP/fvWlHfq5mhfAiEA5YL4B+4CW/foLgUQ/Mk2/1OBsCamwSdXSP8idL31qysCIH/RMV5fJ7syJh49L+Gic4UTUsEaZFw0daG+tVF+kYmbAiAg/Ei/gwKNyzxwWMQPQLAJ1CugcH2o5wmRcTG3i5GiTwIhAKJ9x+A+2Fw2ycyF/QO8PjKortNTlZY7jNJvfvQw0RF7";
    public String extractUsername(String jwt) {
        return extractClaim(jwt,Claims::getSubject);
    }
    public Claims extractAllClaims(String jwt) throws JwtException {
        try {
            Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT Token has expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT Token is unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT Token is malformed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT claims string is empty or invalid: " + e.getMessage());
        }
    }

    public <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        Claims claims;
        try {
            claims = extractAllClaims(jwt);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long nowMillis = System.currentTimeMillis();
        long expirationTimeMillis = nowMillis + (1000 * 60 * 60); // 60 minutes

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expirationTimeMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean isTokenValid(String jwt, UserDetails userdetails)
    {
        try {
            final String email = extractUsername(jwt);
            return (email.equals(userdetails.getUsername()) && !isTokenExpired(jwt));
        }
        catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

    public boolean isTokenExpired(String jwt) {
        try {
            return extractExpiration(jwt).before(new Date());
        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

    public Date extractExpiration(String jwt) {
        try {
            return extractClaim(jwt, Claims::getExpiration);
        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

}