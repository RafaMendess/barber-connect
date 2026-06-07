package com.projeto.barberconnect.dto.offeredService;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumo do serviço oferecido.")
public record OfferedServiceSummaryResponseDto(
        @Schema(description = "Identificador do serviço", example = "5") Long id,
        @Schema(description = "Nome do serviço", example = "Corte") String name,
        @Schema(description = "Preço do serviço", example = "65.00") BigDecimal price,
        @Schema(description = "Duração estimada em minutos", example = "40") Integer duration
) {
}
