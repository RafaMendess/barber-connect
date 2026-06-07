package com.projeto.barberconnect.dto.offeredService;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateOfferedServiceRequestDto(
        @Size(max = 100)
        String name,
        String description,
        @PositiveOrZero
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,
        @Positive
        Integer estimatedTime
) {
}
