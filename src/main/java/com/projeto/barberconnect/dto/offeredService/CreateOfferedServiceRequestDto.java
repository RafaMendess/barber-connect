package com.projeto.barberconnect.dto.offeredService;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateOfferedServiceRequestDto(
        @NotBlank
        @Size(max = 100)
        String name,
        String description,
        @NotNull
        @PositiveOrZero
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,
        @NotNull
        @Positive
        Integer estimatedTime
) {
}
