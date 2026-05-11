package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.auth.AuthResponseDto;
import com.projeto.barberconnect.dto.auth.LoginRequestDto;
import com.projeto.barberconnect.dto.auth.RegisterRequestDto;
import com.projeto.barberconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;


    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDto dto){
        this.service.register(dto);

        return ResponseEntity.ok().build();
    }
    @PostMapping("/login")
    ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto dto){
        return ResponseEntity.ok(this.service.login(dto));
    }
}
