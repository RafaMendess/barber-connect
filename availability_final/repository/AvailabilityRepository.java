package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findAllByBarberIdAndActiveTrue(Long barberId);

    Optional<Availability> findByIdAndActiveTrue(Long id);

    boolean existsByBarberIdAndDayOfWeekAndActiveTrue(Long barberId, Short dayOfWeek);

    Optional<Availability> findByIdAndBarberIdAndActiveTrue(Long id, Long barberId);
}
