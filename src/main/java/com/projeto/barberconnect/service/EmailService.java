package com.projeto.barberconnect.service;

public interface EmailService {
    void sendEmailVerificationCode(String email, String code);
    void sendPasswordResetCode(String email, String code);
}
