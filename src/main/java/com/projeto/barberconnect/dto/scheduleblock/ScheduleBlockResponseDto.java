package com.projeto.barberconnect.dto.scheduleblock;

import java.time.LocalDateTime;

public record ScheduleBlockResponseDto(
        Long id,
        Long barberId,
        String barberName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String reason,
        Boolean active,
        LocalDateTime createdAt
) {
}
