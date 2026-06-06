package com.projeto.barberconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação 1:1 com agendamento — UNIQUE no banco
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Appointment appointment;

    // Banco usa VARCHAR(45) — EnumType.STRING garante compatibilidade
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 45)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 45)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "data_pagamento")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime createdAt;
}
