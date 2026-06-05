package com.projeto.barberconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "barbeiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Barber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbearia_id", nullable = false)
    private Barbershop barbershop;

    @Column(name = "especialidade")
    private String specialty;

    @Column(name = "descricao")
    private String description;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "barbeiro_servico",
            joinColumns = @JoinColumn(name = "barbeiro_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private Set<OfferedService> services = new HashSet<>();

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    private List<Availability> availabilities;

    @CreationTimestamp
    @Column(name = "data_criacao")
    private LocalDateTime createAt;
}
