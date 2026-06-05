package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.availability.AvailabilityResponseDto;
import com.projeto.barberconnect.dto.availability.CreateAvailabilityRequestDto;
import com.projeto.barberconnect.dto.availability.UpdateAvailabilityRequestDto;
import com.projeto.barberconnect.entity.Availability;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.AvailabilityMapper;
import com.projeto.barberconnect.repository.AvailabilityRepository;
import com.projeto.barberconnect.repository.BarberRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final BarberRepository barberRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository, BarberRepository barberRepository) {
        this.availabilityRepository = availabilityRepository;
        this.barberRepository = barberRepository;
    }

    @Transactional
    public AvailabilityResponseDto create(Long barberId, CreateAvailabilityRequestDto dto, Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        assertCanManageAvailability(barber, currentUserId);

        validateTimeWindow(dto.startTime(), dto.endTime());
        validateAvailabilityRules(barberId, dto.dayOfWeek(), dto.startTime(), dto.endTime(), null);

        Availability availability = AvailabilityMapper.toEntity(dto, barber);
        Availability saved = availabilityRepository.save(availability);

        return AvailabilityMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getAllByBarber(Long barberId) {
        findActiveBarberOrThrow(barberId);

        return availabilityRepository.findAllByBarberIdAndActiveTrue(barberId)
                .stream()
                .map(AvailabilityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponseDto getById(Long barberId, Long availabilityId) {
        Availability availability = findActiveAvailabilityOrThrow(barberId, availabilityId);
        return AvailabilityMapper.toResponse(availability);
    }

    @Transactional
    public AvailabilityResponseDto update(Long barberId,
                                          Long availabilityId,
                                          UpdateAvailabilityRequestDto dto,
                                          Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        assertCanManageAvailability(barber, currentUserId);

        Availability availability = findActiveAvailabilityOrThrow(barberId, availabilityId);

        Short dayOfWeek = dto.dayOfWeek() != null ? dto.dayOfWeek() : availability.getDayOfWeek();
        LocalTime startTime = dto.startTime() != null ? dto.startTime() : availability.getStartTime();
        LocalTime endTime = dto.endTime() != null ? dto.endTime() : availability.getEndTime();

        validateTimeWindow(startTime, endTime);
        validateAvailabilityRules(barberId, dayOfWeek, startTime, endTime, availabilityId);

        AvailabilityMapper.applyUpdate(dto, availability);

        return AvailabilityMapper.toResponse(availability);
    }

    @Transactional
    public void delete(Long barberId, Long availabilityId, Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        assertCanManageAvailability(barber, currentUserId);

        Availability availability = findActiveAvailabilityOrThrow(barberId, availabilityId);
        availability.setActive(false);
    }

    private Barber findActiveBarberOrThrow(Long barberId) {
        return barberRepository.findByIdAndActiveTrue(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber with id " + barberId + " not found"));
    }

    private Availability findActiveAvailabilityOrThrow(Long barberId, Long availabilityId) {
        return availabilityRepository.findByIdAndBarberIdAndActiveTrue(availabilityId, barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability with id " + availabilityId + " for barber " + barberId + " not found"));
    }

    private void assertCanManageAvailability(Barber barber, Long currentUserId) {
        boolean isBarberOwner = barber.getUser().getId().equals(currentUserId);
        boolean isBarbershopOwner = barber.getBarbershop().getOwner().getId().equals(currentUserId);

        if (!isBarberOwner && !isBarbershopOwner) {
            throw new AccessDeniedException("You do not have permission to manage this availability");
        }
    }

    private void validateTimeWindow(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("Start time must be before end time");
        }
    }

    private void validateAvailabilityRules(Long barberId,
                                           Short dayOfWeek,
                                           LocalTime startTime,
                                           LocalTime endTime,
                                           Long currentAvailabilityId) {
        if (dayOfWeek == null) {
            throw new BusinessException("Day of week is required");
        }

        boolean duplicateExists = currentAvailabilityId == null
                ? availabilityRepository.existsByBarberIdAndDayOfWeekAndStartTimeAndEndTimeAndActiveTrue(
                        barberId, dayOfWeek, startTime, endTime)
                : availabilityRepository.existsByBarberIdAndDayOfWeekAndStartTimeAndEndTimeAndActiveTrueAndIdNot(
                        barberId, dayOfWeek, startTime, endTime, currentAvailabilityId);

        if (duplicateExists) {
            throw new BusinessException("Barber already has an identical availability for this day and time");
        }

        List<Availability> sameDayAvailabilities = currentAvailabilityId == null
                ? availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(barberId, dayOfWeek)
                : availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrueAndIdNot(barberId, dayOfWeek, currentAvailabilityId);

        boolean overlaps = sameDayAvailabilities.stream()
                .anyMatch(existing -> overlaps(startTime, endTime, existing.getStartTime(), existing.getEndTime()));

        if (overlaps) {
            throw new BusinessException("Barber already has an overlapping availability for this day");
        }
    }

    private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
