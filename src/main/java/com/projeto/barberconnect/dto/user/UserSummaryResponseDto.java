package com.projeto.barberconnect.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo do usuário.")
public record UserSummaryResponseDto(
        @Schema(description = "Identificador do usuário", example = "10") Long id,
        @Schema(description = "Nome do usuário", example = "Rafael") String name
) {
}
