package com.projeto.barberconnect.dto.scheduleblock;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo do bloqueio de agenda.")
public record ScheduleBlockSummaryResponseDto(
        @Schema(description = "Identificador do bloqueio de agenda", example = "12") Long id,
        @Schema(description = "Data e hora de início do bloqueio", example = "2026-06-15T09:00:00") LocalDateTime startDateTime,
        @Schema(description = "Data e hora de término do bloqueio", example = "2026-06-15T12:00:00") LocalDateTime endDateTime,
        @Schema(description = "Motivo do bloqueio", example = "Manutenção") String reason,
        @Schema(description = "Se o bloqueio ainda está ativo", example = "true") Boolean active
) {
}
