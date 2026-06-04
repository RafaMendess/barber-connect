package com.projeto.barberconnect.dto.availability;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

public record UpdateAvailabilityRequestDto(

        @Min(value = 0, message = "Day of week must be between 0 (Sunday) and 6 (Saturday)")
        @Max(value = 6, message = "Day of week must be between 0 (Sunday) and 6 (Saturday)")
        Short dayOfWeek,

        LocalTime startTime,

        LocalTime endTime
) {
}
