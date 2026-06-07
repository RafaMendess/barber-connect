package com.projeto.barberconnect.dto.availability;

import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Detalhes de uma disponibilidade de barbeiro.")
public record AvailabilityResponseDto(
        @Schema(description = "Identificador da disponibilidade", example = "7") Long id,
        @Schema(description = "Barbeiro associado à disponibilidade") BarberSummaryResponseDto barber,
        @Schema(description = "Dia da semana (1=segunda a 7=domingo)", example = "1") Short dayOfWeek,
        @Schema(description = "Hora de início da disponibilidade", example = "09:00:00") LocalTime startTime,
        @Schema(description = "Hora de término da disponibilidade", example = "18:00:00") LocalTime endTime,
        @Schema(description = "Se a disponibilidade está ativa", example = "true") Boolean active
) {
}
