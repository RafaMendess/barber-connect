package com.projeto.barberconnect.dto.availability;

import java.time.LocalTime;

public record AvailabilityResponseDto(
        Long id,
        Long barberId,
        String barberName,
        Short dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean active
) {
}
