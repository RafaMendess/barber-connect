package com.projeto.barberconnect.dto.auth;

import java.util.Set;

public record UserAuthResponseDto(Long id, String name, String email, Set<String> roles) {
}
