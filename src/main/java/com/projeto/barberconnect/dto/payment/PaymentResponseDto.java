package com.projeto.barberconnect.dto.payment;

import com.projeto.barberconnect.entity.PaymentStatus;
import com.projeto.barberconnect.entity.PaymentType;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long appointmentId,
        String clientName,
        String serviceName,
        String barberName,
        PaymentType type,
        PaymentStatus status,
        LocalDateTime paymentDate,
        LocalDateTime createdAt
) {
}
