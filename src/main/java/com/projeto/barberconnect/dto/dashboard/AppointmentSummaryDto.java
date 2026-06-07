package com.projeto.barberconnect.dto.dashboard;

import com.projeto.barberconnect.entity.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentSummaryDto(
        Long id,
        LocalDateTime appointmentDateTime,
        LocalDateTime endsAt,
        AppointmentStatus status,
        String clientName,
        String barberName,
        String serviceName,
        Integer serviceDurationMinutes
) {
}
