package com.projeto.barberconnect.security.jwt;

import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-minutes:60}")
    private long expirationMinutes;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret must have at least 32 bytes");
        }

        if (expirationMinutes <= 0) {
            throw new IllegalStateException("jwt.expiration-minutes must be greater than zero");
        }
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + getExpirationDuration().toMillis());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return Jwts.parser().
                verifyWith(getSignInKey()).
                build().
                parseSignedClaims(token).
                getPayload().
                getSubject();
    }
    public boolean isValidToken(String token, UserDetails userDetails){
        String username= this.extractUsername(token);

        return username.equals(userDetails.getUsername()) && userDetails.isEnabled();
    }

    public long getExpirationInSeconds() {
        return getExpirationDuration().toSeconds();
    }

    private Duration getExpirationDuration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
