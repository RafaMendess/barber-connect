package com.projeto.barberconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueio_agenda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Barber barber;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "fim", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "motivo", nullable = false)
    private String reason;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime createdAt;
}
