package com.opengym.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${opengym.data-dir:./data}") String dataDir,
            @Value("${opengym.jwt.expiration-days:90}") int expirationDays
    ) throws IOException {
        this.expirationMs = expirationDays * 86400000L;
        Path secretFile = Path.of(dataDir, "jwt-secret");
        Files.createDirectories(Path.of(dataDir));

        String encoded;
        if (Files.exists(secretFile)) {
            encoded = Files.readString(secretFile).trim();
        } else {
            byte[] secret = new byte[64];
            new SecureRandom().nextBytes(secret);
            encoded = Base64.getEncoder().encodeToString(secret);
            Files.writeString(secretFile, encoded);
        }
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encoded));
    }

    /** Generate a JWT for the given user ID. */
    public String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Extract user ID from a JWT. Returns null if invalid/expired. */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
