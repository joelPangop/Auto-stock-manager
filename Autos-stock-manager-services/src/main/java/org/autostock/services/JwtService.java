package org.autostock.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.autostock.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-expiration}") long accessExpirationMs,
            @Value("${security.jwt.refresh-expiration}") long refreshExpirationMs) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret manquant (configure application.properties ou variable d'environnement)");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpirationMs, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpirationMs, "refresh");
    }

    private String buildToken(User user, long expiresIn, String type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(Map.of(
                        "uid", user.getId(),
                        "role", user.getRole().name(),
                        "typ", type
                ))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiresIn))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public boolean isRefreshToken(String token) {
        try {
            var claims = parse(token).getBody();
            return "refresh".equals(claims.get("typ", String.class));
        } catch (Exception e) { return false; }
    }

    public long getAccessExpirationMs() { return accessExpirationMs; }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
