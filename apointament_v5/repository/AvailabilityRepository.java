package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    // Retorna todas as disponibilidades ativas de um barbeiro
    List<Availability> findAllByBarberIdAndActiveTrue(Long barberId);

    // Busca por id e que esteja ativo
    Optional<Availability> findByIdAndActiveTrue(Long id);

    // Verifica duplicidade de dia para o mesmo barbeiro
    boolean existsByBarberIdAndDayOfWeekAndActiveTrue(Long barberId, Short dayOfWeek);

    // Busca por id + barbeiro (garante que o registro pertence ao barbeiro)
    Optional<Availability> findByIdAndBarberIdAndActiveTrue(Long id, Long barberId);

    // Usado pelo AppointmentService para validar horário de atendimento
    Optional<Availability> findByBarberIdAndDayOfWeekAndActiveTrue(Long barberId, Short dayOfWeek);
}
