package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findAllByBarberIdAndActiveTrue(Long barberId);

    List<Availability> findAllByBarberIdAndDayOfWeekAndActiveTrue(Long barberId, Short dayOfWeek);

    List<Availability> findAllByBarberIdAndDayOfWeekAndActiveTrueAndIdNot(Long barberId, Short dayOfWeek, Long id);

    Optional<Availability> findByIdAndBarberIdAndActiveTrue(Long id, Long barberId);

    boolean existsByBarberIdAndDayOfWeekAndStartTimeAndEndTimeAndActiveTrue(Long barberId,
                                                                            Short dayOfWeek,
                                                                            LocalTime startTime,
                                                                            LocalTime endTime);

    boolean existsByBarberIdAndDayOfWeekAndStartTimeAndEndTimeAndActiveTrueAndIdNot(Long barberId,
                                                                                    Short dayOfWeek,
                                                                                    LocalTime startTime,
                                                                                    LocalTime endTime,
                                                                                    Long id);
}
