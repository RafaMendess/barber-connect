package com.projeto.barberconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;


@Entity
@Table(name = "barbearia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Barbershop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "telefone", length = 20)
    private String phone;

    @Column(name = "endereco", nullable = false)
    private String address;

    @Column(name = "horario_funcionamento", length = 100)
    private String businessHours;

    @Column(name = "foto_url")
    private String photoUrl;

    @Column(name = "localizacao", columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point location;

    @Column(name = "ativo", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
