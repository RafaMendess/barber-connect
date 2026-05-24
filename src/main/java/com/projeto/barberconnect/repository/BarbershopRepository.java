package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarbershopRepository extends JpaRepository<Barbershop, Long> {
    boolean existsByCnpj(String cnpj);

    Optional<Barbershop> findByIdAndActiveTrue(Long id);

    List<Barbershop> findAllByActiveTrue();
}
