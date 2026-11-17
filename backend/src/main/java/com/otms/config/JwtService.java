
package com.otms.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;
    private final Duration refreshTtl = Duration.ofDays(7);

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(padSecret(secret));
        this.ttl = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(String email, String name, String role) {
        return generate(email, name, role, ttl, "access");
    }

    public String generateRefreshToken(String email, String name, String role) {
        return generate(email, name, role, refreshTtl, "refresh");
    }

    private String generate(String email, String name, String role,
                            Duration duration, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("name", name)
                .claim("role", role)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(duration)))
                .signWith(key)
                .compact();
    }

    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<Claims> parseRefreshToken(String token) {
        return parse(token).filter(
                claims -> "refresh".equals(claims.get("type", String.class))
        );
    }

    private byte[] padSecret(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        byte[] padded = new byte[32];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }
}
