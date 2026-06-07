package com.projeto.barberconnect.dto.appointment;

import com.projeto.barberconnect.entity.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDto(
        Long id,
        LocalDateTime appointmentDateTime,
        LocalDateTime endsAt,
        AppointmentStatus status,
        String observation,
        Long clientId,
        String clientName,
        Long barberId,
        String barberName,
        Long serviceId,
        String serviceName,
        Integer serviceDurationMinutes,
        LocalDateTime createdAt
) {
}
