package com.projeto.barberconnect.dto.barbershop;

import java.math.BigDecimal;

public record BarbershopResponseDto(
        Long id,
        String name,
        String cnpj,
        String phone,
        String address,
        String businessHours,
        String photoUrl,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
