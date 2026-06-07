package com.projeto.barberconnect.dto.payment;

import com.projeto.barberconnect.dto.appointment.AppointmentSummaryResponseDto;
import com.projeto.barberconnect.entity.PaymentStatus;
import com.projeto.barberconnect.entity.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de um pagamento.")
public record PaymentResponseDto(
        @Schema(description = "Identificador do pagamento", example = "20") Long id,
        @Schema(description = "Informação resumida do agendamento associado") AppointmentSummaryResponseDto appointment,
        @Schema(description = "Tipo de pagamento") PaymentType type,
        @Schema(description = "Status do pagamento") PaymentStatus status,
        @Schema(description = "Data do pagamento") LocalDateTime paymentDate,
        @Schema(description = "Data de criação do registro do pagamento") LocalDateTime createdAt
) {
}
