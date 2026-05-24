package com.projeto.barberconnect.dto.barber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBarberRequestDto(
        @NotNull
        Long userId,
        
        @Size(max = 255)
        String specialty,

        String description
) {
}
