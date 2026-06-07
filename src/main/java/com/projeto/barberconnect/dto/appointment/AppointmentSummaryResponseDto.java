package com.projeto.barberconnect.dto.appointment;

import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceSummaryResponseDto;
import com.projeto.barberconnect.dto.user.UserSummaryResponseDto;
import com.projeto.barberconnect.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo de um agendamento.")
public record AppointmentSummaryResponseDto(
        @Schema(description = "Identificador do agendamento", example = "1") Long id,
        @Schema(description = "Data e hora agendada", example = "2026-06-20T14:00:00") LocalDateTime appointmentDateTime,
        @Schema(description = "Data e hora estimada de fim", example = "2026-06-20T14:45:00") LocalDateTime endsAt,
        @Schema(description = "Status do agendamento") AppointmentStatus status,
        @Schema(description = "Cliente do agendamento") UserSummaryResponseDto client,
        @Schema(description = "Barbeiro do agendamento") BarberSummaryResponseDto barber,
        @Schema(description = "Serviço associado ao agendamento") OfferedServiceSummaryResponseDto service
) {
}
