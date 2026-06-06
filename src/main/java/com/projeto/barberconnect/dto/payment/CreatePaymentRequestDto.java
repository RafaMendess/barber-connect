package com.projeto.barberconnect.dto.payment;

import com.projeto.barberconnect.entity.PaymentType;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestDto(

        @NotNull(message = "Appointment id is required")
        Long appointmentId,

        @NotNull(message = "Payment type is required")
        PaymentType type
) {
}
