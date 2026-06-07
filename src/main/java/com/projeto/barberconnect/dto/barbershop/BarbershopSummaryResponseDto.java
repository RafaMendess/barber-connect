package com.projeto.barberconnect.dto.barbershop;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo da barbearia.")
public record BarbershopSummaryResponseDto(
        @Schema(description = "Identificador da barbearia", example = "3") Long id,
        @Schema(description = "Nome da barbearia", example = "Barber Connect") String name
) {
}
