package com.projeto.barberconnect.dto.barber;

import jakarta.validation.constraints.Size;

public record UpdateBarberRequestDto(
        @Size(max=255)
        String specialty,

        String description
) {
}
