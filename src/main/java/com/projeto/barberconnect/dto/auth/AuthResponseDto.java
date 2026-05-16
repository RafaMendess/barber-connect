package com.projeto.barberconnect.dto.auth;

public record AuthResponseDto(
        String token,
        String tokenType,
        long expiresInSeconds
) {
}
