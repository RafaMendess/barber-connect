package com.projeto.barberconnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationCodeRequestDto(
        @NotBlank
        @Email
        String email
) {
}
