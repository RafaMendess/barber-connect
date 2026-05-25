package com.projeto.barberconnect.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
        @NotBlank
        String refreshToken
        )

{
}
