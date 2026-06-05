package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByClientIdOrderByAppointmentDateTimeDesc(Long clientId);

    List<Appointment> findAllByBarberIdOrderByAppointmentDateTimeDesc(Long barberId);

    List<Appointment> findAllByBarberIdAndStatus(Long barberId, AppointmentStatus status);

    // Detecta sobreposição de horários para o barbeiro.
    // Dois intervalos [A, B) e [C, D) se sobrepõem quando: A < D && C < B
    // Onde: A = appointmentDateTime existente, B = A + estimatedTime (em minutos)
    //       C = :newStart,                    D = :newEnd
    // Ignora agendamentos já cancelados ou concluídos.
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.barber.id     = :barberId
              AND a.status NOT IN :ignoredStatuses
              AND a.appointmentDateTime < :newEnd
              AND :newStart < a.appointmentDateTime
                    + FUNCTION('make_interval', 0, 0, 0, 0, 0, a.service.estimatedTime, 0)
            """)
    boolean existsConflict(
            @Param("barberId") Long barberId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd")   LocalDateTime newEnd,
            @Param("ignoredStatuses") List<AppointmentStatus> ignoredStatuses
    );

    // Mesma lógica acima, porém exclui o próprio agendamento (para uso no update)
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.barber.id     = :barberId
              AND a.id           <> :excludeId
              AND a.status NOT IN :ignoredStatuses
              AND a.appointmentDateTime < :newEnd
              AND :newStart < a.appointmentDateTime
                    + FUNCTION('make_interval', 0, 0, 0, 0, 0, a.service.estimatedTime, 0)
            """)
    boolean existsConflictExcluding(
            @Param("barberId")        Long barberId,
            @Param("newStart")        LocalDateTime newStart,
            @Param("newEnd")          LocalDateTime newEnd,
            @Param("excludeId")       Long excludeId,
            @Param("ignoredStatuses") List<AppointmentStatus> ignoredStatuses
    );

    // Usado pelo Dashboard: agendamentos do barbeiro em um intervalo de datas
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.barber.id              = :barberId
              AND a.appointmentDateTime   >= :start
              AND a.appointmentDateTime    < :end
            ORDER BY a.appointmentDateTime ASC
            """)
    List<Appointment> findAllByBarberIdAndDateRange(
            @Param("barberId") Long barberId,
            @Param("start")    LocalDateTime start,
            @Param("end")      LocalDateTime end
    );

    // Usado pelo Dashboard: total de agendamentos por barbearia em um período
    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.barber.barbershop.id   = :barbershopId
              AND a.appointmentDateTime   >= :start
              AND a.appointmentDateTime    < :end
              AND a.status                 = :status
            """)
    Long countByBarbershopAndPeriodAndStatus(
            @Param("barbershopId") Long barbershopId,
            @Param("start")        LocalDateTime start,
            @Param("end")          LocalDateTime end,
            @Param("status")       AppointmentStatus status
    );
}
