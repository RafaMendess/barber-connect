package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.auth.*;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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

    @GetMapping("/me")
    ResponseEntity<UserAuthResponseDto> me(Authentication authentication){
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new UserAuthResponseDto(user.getId(), user.getName(),user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponseDto> refresh(@RequestBody @Valid RefreshTokenRequestDto dto){
        return ResponseEntity.ok(this.service.refresh(dto));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequestDto dto){
        this.service.logout(dto);

        return ResponseEntity.noContent().build();
    }
    @PostMapping("/verify-email")
    ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequestDto dto){
        this.service.verifyEmail(dto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification-code")
    ResponseEntity<Void> resendVerificationCode(@RequestBody @Valid ResendVerificationCodeRequestDto dto){
        this.service.resendVerificationCode(dto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto dto){
        this.service.forgotPassword(dto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto dto){
        this.service.resetPassword(dto);

        return ResponseEntity.noContent().build();
    }
}
