package com.fleet_management_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(String secret, long accessExpirationMs) {
        this(secret, accessExpirationMs, 7L * 24 * 60 * 60 * 1000); // default 7 jours
    }

    public JwtService(String secret, long accessExpirationMs, long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Map<String, Object> finalClaims = new HashMap<>(claims != null ? claims : Map.of());
        finalClaims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        return buildToken(subject, finalClaims, accessExpirationMs);
    }

    /**
     * Refresh token JWT (long durée). DTO/endpoint/storage ماشي من مسؤولية هاد الكلاس.
     */
    public String generateRefreshToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        return buildToken(subject, claims, refreshExpirationMs);
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        return isTokenValid(token, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, REFRESH_TOKEN_TYPE);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    private boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            if (exp != null && exp.before(new Date())) return false;

            Object type = claims.get(TOKEN_TYPE_CLAIM);
            return expectedType.equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
