package com.projeto.barberconnect.security.jwt;

import com.projeto.barberconnect.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + 1000 * 60 * 60
        );
        return Jwts.builder()
                .subject(user.getEmail())
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

        return username.equals(userDetails.getUsername());
    }
}
