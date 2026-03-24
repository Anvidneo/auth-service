package com.bank_dugongo.auth_service.security;

import io.jsonwebtoken.Claims;           // Para leer datos del token
import io.jsonwebtoken.Jwts;             // Constructor principal de JWT
import io.jsonwebtoken.security.Keys;    // Para generar la clave secreta

import org.springframework.beans.factory.annotation.Value; // Leer properties
import org.springframework.stereotype.Component;           // Anotación

import javax.crypto.SecretKey;          // Tipo de la clave secreta
import java.nio.charset.StandardCharsets; // Para convertir String a bytes
import java.util.Date;                   // Para fechas de creación/expiración
import java.util.HashMap;                // Para claims adicionales
import java.util.Map;                    // Interface de Map
import java.util.function.Function;      // Para funciones lambda

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Método para generar la clave de firma a partir del secreto
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Integer userId, Integer customerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("customerId", customerId);

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSigningKey())
            .compact();
    }

    // Métodos para extraer información del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public Integer extractCustomerId(String token) {
        return extractClaim(token, claims -> claims.get("customerId", Integer.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long getExpirationTime() {
        return expiration;
    }

    // Métodos para validar el token
    private Boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate.before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
