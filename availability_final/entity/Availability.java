package com.projeto.barberconnect.entity;

import jakarta.persistence.*;
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

    // 0 = Domingo, 1 = Segunda, 2 = Terça, 3 = Quarta,
    // 4 = Quinta,  5 = Sexta,   6 = Sábado
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
