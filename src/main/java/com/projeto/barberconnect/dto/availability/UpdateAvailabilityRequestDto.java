package com.projeto.barberconnect.dto.availability;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

public record UpdateAvailabilityRequestDto(
        @Min(value = 1, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
        @Max(value = 7, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
        Short dayOfWeek,

        LocalTime startTime,

        LocalTime endTime
) {
}
