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

    List<Appointment> findAllByBarberIdAndStatusNotInAndAppointmentDateTimeLessThan(
            Long barberId,
            List<AppointmentStatus> ignoredStatuses,
            LocalDateTime newEnd
    );

    List<Appointment> findAllByBarberIdAndIdNotAndStatusNotInAndAppointmentDateTimeLessThan(
            Long barberId,
            Long excludeId,
            List<AppointmentStatus> ignoredStatuses,
            LocalDateTime newEnd
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.barber.id = :barberId
              AND a.appointmentDateTime >= :start
              AND a.appointmentDateTime < :end
            ORDER BY a.appointmentDateTime ASC
            """)
    List<Appointment> findAllByBarberIdAndDateRange(@Param("barberId") Long barberId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.barber.barbershop.id = :barbershopId
              AND a.appointmentDateTime >= :start
              AND a.appointmentDateTime < :end
              AND a.status = :status
            """)
    Long countByBarbershopAndPeriodAndStatus(@Param("barbershopId") Long barbershopId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("status") AppointmentStatus status);
}
