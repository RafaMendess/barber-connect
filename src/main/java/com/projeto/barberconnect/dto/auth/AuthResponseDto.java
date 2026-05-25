package com.projeto.barberconnect.dto.auth;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
