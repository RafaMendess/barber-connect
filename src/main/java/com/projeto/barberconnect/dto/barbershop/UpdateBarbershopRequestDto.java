package com.projeto.barberconnect.dto.barbershop;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateBarbershopRequestDto(
        @Size(max = 100)
        String name,

        @Size(max = 20)
        String phone,

        @Size(max = 255)
        String address,

        @Size(max = 100)
        String businessHours,

        @Size(max = 255)
        String photoUrl,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude
) { }
