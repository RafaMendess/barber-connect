package com.projeto.barberconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferedService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(name = "descricao")
    private String description;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duracao_estimada", nullable = false)
    private Integer estimatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbearia_id", nullable = false)
    private Barbershop barbershop;

    @ManyToMany(mappedBy = "services")
    private Set<Barber> barbers = new HashSet<>();

    @Column(name = "ativo", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime createdAt;

}
