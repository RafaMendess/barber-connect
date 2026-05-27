package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    boolean existsByUserIdAndActiveTrue(Long userId);
    Optional<Barber> findByIdAndActiveTrue(Long id);

    List<Barber> findAllByBarbershopIdAndActiveTrue(Long id);

    Optional<Barber> findByUserIdAndActiveTrue(Long userId);

    Optional<Barber> findByIdAndBarbershopIdAndActiveTrue(Long id, Long barbershopId);
}
