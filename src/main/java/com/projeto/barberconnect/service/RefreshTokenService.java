package com.projeto.barberconnect.service;


import com.projeto.barberconnect.entity.RefreshToken;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.InvalidRefreshTokenException;
import com.projeto.barberconnect.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final SecureRandom  random = new SecureRandom();
    private final RefreshTokenRepository  refreshTokenRepository;

    @Value("${refresh-token.expiration-days:7}")
    private long expirationDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Transactional
    public String createRefreshToken(User user) {
        String rawToken =generateRawToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User validateAndRotate(String rawToken) {
        RefreshToken refreshToken = findValidToken(rawToken);

        refreshToken.revoke();

        return refreshToken.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).
                ifPresent(RefreshToken::revoke);
    }

    @Transactional
    public void revokeAllFromUser(User user){
        refreshTokenRepository.findByUserAndRevokedAtIsNull(user).
                forEach(RefreshToken::revoke);
    }

    private RefreshToken findValidToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash).
                orElseThrow(()->new InvalidRefreshTokenException("Invalid refresh token"));

        if(!refreshToken.isActive()){
            throw new InvalidRefreshTokenException("Invalid refresh token") ;
        }

        return refreshToken;
    }
    private String generateRawToken(){
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String  rawToken){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash refresh token", e);
        }
    }
}
