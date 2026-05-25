package com.projeto.barberconnect.dto.barbershop;

import jakarta.validation.constraints.*;


import java.math.BigDecimal;

public record CreateBarbershopRequestDto(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(min = 14,max = 19)
        String cnpj,

        @Size(max = 20)
        String phone,

        @NotBlank
        @Size(max = 255)
        String address,

        @Size(max = 100)
        String businessHours,

        @Size(max = 255)
        String photoUrl,

        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude
) {

}
