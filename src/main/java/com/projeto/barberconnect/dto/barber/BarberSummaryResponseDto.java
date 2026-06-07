package com.projeto.barberconnect.dto.barber;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo do barbeiro.")
public record BarberSummaryResponseDto(
        @Schema(description = "Identificador do barbeiro", example = "2") Long id,
        @Schema(description = "Nome do barbeiro", example = "Pedro") String name,
        @Schema(description = "Especialidade do barbeiro", example = "Corte") String specialty
) {
}
