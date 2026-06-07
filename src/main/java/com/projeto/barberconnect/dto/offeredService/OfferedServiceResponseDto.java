package com.projeto.barberconnect.dto.offeredService;

import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import com.projeto.barberconnect.dto.barbershop.BarbershopSummaryResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Detalhes de um serviço oferecido.")
public record OfferedServiceResponseDto(
        @Schema(description = "Identificador do serviço", example = "5") Long id,
        @Schema(description = "Nome do serviço", example = "Corte") String name,
        @Schema(description = "Descrição do serviço", example = "Corte tradicional com finalização") String description,
        @Schema(description = "Preço do serviço", example = "65.00") BigDecimal price,
        @Schema(description = "Duração estimada em minutos", example = "40") Integer estimatedTime,
        @Schema(description = "Barbearia que oferece este serviço") BarbershopSummaryResponseDto barbershop,
        @Schema(description = "Barbeiros associados a este serviço") List<BarberSummaryResponseDto> barbers
) {
}
