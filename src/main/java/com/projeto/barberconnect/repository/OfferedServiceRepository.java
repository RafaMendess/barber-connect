package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.OfferedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {
    List<OfferedService> findAllByBarbershopIdAndActiveTrue(Long barbershopId);

    Optional<OfferedService> findByIdAndActiveTrue(Long id);

    boolean existsByIdAndBarbershopIdAndActiveTrue(Long id, Long barbershopId);

    Optional<OfferedService> findByIdAndBarbershopIdAndActiveTrue(Long id, Long barbershopId);

    boolean existsByIdAndActiveTrue(Long serviceId);
}
