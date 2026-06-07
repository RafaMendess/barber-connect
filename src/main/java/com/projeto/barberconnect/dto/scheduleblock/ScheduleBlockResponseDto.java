package com.projeto.barberconnect.dto.scheduleblock;

import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de um bloqueio de agenda.")
public record ScheduleBlockResponseDto(
        @Schema(description = "Identificador do bloqueio de agenda", example = "12") Long id,
        @Schema(description = "Barbeiro afetado pelo bloqueio") BarberSummaryResponseDto barber,
        @Schema(description = "Data e hora de início do bloqueio", example = "2026-06-20T09:00:00") LocalDateTime startDateTime,
        @Schema(description = "Data e hora de término do bloqueio", example = "2026-06-20T12:00:00") LocalDateTime endDateTime,
        @Schema(description = "Motivo do bloqueio", example = "Reunião interna") String reason,
        @Schema(description = "Se o bloqueio está ativo", example = "true") Boolean active,
        @Schema(description = "Data de criação do registro do bloqueio") LocalDateTime createdAt
) {
}
