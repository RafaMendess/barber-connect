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
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Barber barber;

    @Column(name = "dia_semana", nullable = false)
    private Short dayOfWeek;

    @Column(name = "horario_inicio", nullable = false)
    private LocalTime startTime;

    @Column(name = "horario_fim", nullable = false)
    private LocalTime endTime;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime createdAt;
}
