package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

    List<ScheduleBlock> findAllByBarberIdAndActiveTrue(Long barberId);

    Optional<ScheduleBlock> findByIdAndBarberIdAndActiveTrue(Long id, Long barberId);

    // Verifica se existe bloqueio ativo que sobreponha o intervalo informado
    // Usado pelo AppointmentService antes de agendar
    @Query("""
            SELECT COUNT(b) > 0 FROM ScheduleBlock b
            WHERE b.barber.id  = :barberId
              AND b.active      = true
              AND b.startDateTime < :end
              AND :start          < b.endDateTime
            """)
    boolean existsOverlap(
            @Param("barberId") Long barberId,
            @Param("start")    LocalDateTime start,
            @Param("end")      LocalDateTime end
    );
}
