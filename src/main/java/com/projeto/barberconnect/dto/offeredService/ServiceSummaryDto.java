package com.projeto.barberconnect.dto.offeredService;

import java.math.BigDecimal;

public record ServiceSummaryDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer estimatedTime
) {
}
