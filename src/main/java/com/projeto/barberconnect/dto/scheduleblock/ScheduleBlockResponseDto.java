package com.projeto.barberconnect.dto.scheduleblock;

import java.time.LocalDateTime;

public record ScheduleBlockResponseDto(
        Long id,
        Long barberId,
        String barberName,
        LocalDateTime start,
        LocalDateTime end,
        String reason,
        Boolean active,
        LocalDateTime createdAt
) {
}
