package com.projeto.barberconnect.dto.barber;

public record BarberResponseDto(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String specialty,
        String description
) {
}
