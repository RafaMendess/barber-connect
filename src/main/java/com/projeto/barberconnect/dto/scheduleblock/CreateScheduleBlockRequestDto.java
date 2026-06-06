package com.projeto.barberconnect.dto.scheduleblock;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateScheduleBlockRequestDto(

        @NotNull(message = "Start date and time is required")
        @Future(message = "Start must be a future date and time")
        LocalDateTime start,

        @NotNull(message = "End date and time is required")
        @Future(message = "End must be a future date and time")
        LocalDateTime end,

        String reason
) {
}
