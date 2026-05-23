package com.projeto.barberconnect.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class ConsoleEmailService implements EmailService{
    @Override
    public void sendEmailVerificationCode(String email, String code) {
        System.out.println("[DEV] Email verification code for " + email +": " +code);
    }

    @Override
    public void sendPasswordResetCode(String email, String code) {
        System.out.println("[DEV] Sending password reset code for " + email +": " +code);
    }
}
