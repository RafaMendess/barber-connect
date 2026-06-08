package com.projeto.barberconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:no-reply@barberconnect.local}}")
    private String from;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailVerificationCode(String email, String code) {
        log.info("Verificacao de email enviado para: " + email+"/nCodigo OTP:"+ code);
//        send(email, "BarberConnect - Confirmacao de email",
//                "Seu codigo de confirmacao e: " + code);
    }

    @Override
    public void sendPasswordResetCode(String email, String code) {
        log.info("Mudança de senha enviado para: " + email+"/nCodigo OTP:"+ code);
       // send(email, "BarberConnect - Recuperacao de senha",
               // "Seu codigo de recuperacao de senha e: " + code);

    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
