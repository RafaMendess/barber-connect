package com.projeto.barberconnect.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Dados de autenticação do usuário.")
public record UserAuthResponseDto(
        @Schema(description = "Identificador do usuário", example = "10") Long id,
        @Schema(description = "Nome do usuário", example = "Rafael") String name,
        @Schema(description = "Email do usuário", example = "rafael@example.com") String email,
        @Schema(description = "Perfis de acesso do usuário", example = "[\"CLIENT\", \"SHOP_OWNER\"]") Set<String> roles
) {
}
