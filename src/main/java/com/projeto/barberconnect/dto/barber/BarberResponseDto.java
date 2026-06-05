package com.projeto.barberconnect.dto.barber;

import com.projeto.barberconnect.dto.offeredService.ServiceSummaryDto;

import java.util.List;

public record BarberResponseDto(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String specialty,
        String description,
        List<ServiceSummaryDto> services
) {
}
