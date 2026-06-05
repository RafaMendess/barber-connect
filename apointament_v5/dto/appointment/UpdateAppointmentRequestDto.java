package com.projeto.barberconnect.dto.appointment;

import com.projeto.barberconnect.entity.AppointmentStatus;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public record UpdateAppointmentRequestDto(

        @Future(message = "Appointment must be scheduled for a future date and time")
        LocalDateTime appointmentDateTime,

        AppointmentStatus status,

        String notes
) {
}
