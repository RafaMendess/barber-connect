package com.projeto.barberconnect.dto.offeredService;

import com.projeto.barberconnect.dto.barbershop.BarbershopResponseDto;

import java.math.BigDecimal;

public record OfferedServiceResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer estimatedTime,
        BarbershopResponseDto barbershop
) {
}
