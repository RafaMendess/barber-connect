package com.projeto.barberconnect.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAppointmentRequestDto(

        @NotNull(message = "Appointment date and time is required")
        @Future(message = "Appointment must be scheduled for a future date and time")
        LocalDateTime appointmentDateTime,

        @NotNull(message = "Barber id is required")
        Long barberId,

        @NotNull(message = "Service id is required")
        Long serviceId,

        String notes
) {
}
