package com.projeto.barberconnect.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do agendamento.")
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
