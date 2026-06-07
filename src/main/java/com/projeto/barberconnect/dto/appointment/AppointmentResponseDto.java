package com.projeto.barberconnect.dto.appointment;

import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceSummaryResponseDto;
import com.projeto.barberconnect.dto.user.UserSummaryResponseDto;
import com.projeto.barberconnect.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de um agendamento.")
public record AppointmentResponseDto(
        @Schema(description = "Identificador do agendamento", example = "1") Long id,
        @Schema(description = "Data e hora agendada", example = "2026-06-20T14:00:00") LocalDateTime appointmentDateTime,
        @Schema(description = "Data e hora estimada de término", example = "2026-06-20T14:45:00") LocalDateTime endsAt,
        @Schema(description = "Status do agendamento") AppointmentStatus status,
        @Schema(description = "Observações adicionais do cliente", example = "Cortar com pouco volume") String observation,
        @Schema(description = "Cliente do agendamento") UserSummaryResponseDto client,
        @Schema(description = "Barbeiro responsável pelo agendamento") BarberSummaryResponseDto barber,
        @Schema(description = "Serviço selecionado para o agendamento") OfferedServiceSummaryResponseDto service,
        @Schema(description = "Data de criação do registro do agendamento") LocalDateTime createdAt
) {
}
