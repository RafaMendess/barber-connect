package com.projeto.barberconnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequestDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String code
) {
}
