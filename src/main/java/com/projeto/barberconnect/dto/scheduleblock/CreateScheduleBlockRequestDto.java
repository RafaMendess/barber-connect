package com.projeto.barberconnect.dto.scheduleblock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateScheduleBlockRequestDto(

        @NotNull(message = "Start date and time is required")
        LocalDateTime start,

        @NotNull(message = "End date and time is required")
        LocalDateTime end,

        @NotBlank(message = "Reason is required")
        String reason
) {
}
