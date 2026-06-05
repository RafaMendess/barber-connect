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

    public AvailabilityService(AvailabilityRepository availabilityRepository,
                               BarberRepository barberRepository) {
        this.availabilityRepository = availabilityRepository;
        this.barberRepository = barberRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Transactional
    public AvailabilityResponseDto create(Long barberId,
                                          CreateAvailabilityRequestDto dto,
                                          Long currentUserId) {

        Barber barber = findActiveBarberOrThrow(barberId);

        checkBarberOrOwnerAccess(barber, currentUserId);

        if (!dto.startTime().isBefore(dto.endTime())) {
            throw new BusinessException("Start time must be before end time");
        }

        if (availabilityRepository.existsByBarberIdAndDayOfWeekAndActiveTrue(
                barberId, dto.dayOfWeek())) {
            throw new BusinessException(
                    "Barber already has an availability registered for day " + dto.dayOfWeek());
        }

        Availability availability = AvailabilityMapper.toEntity(dto, barber);
        return AvailabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getAllByBarber(Long barberId) {
        // Usa findByIdAndActiveTrue — igual ao padrão do BarberService.getById()
        findActiveBarberOrThrow(barberId);

        return availabilityRepository
                .findAllByBarberIdAndActiveTrue(barberId)
                .stream()
                .map(AvailabilityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponseDto getById(Long barberId, Long availabilityId) {
        Availability availability = availabilityRepository
                .findByIdAndBarberIdAndActiveTrue(availabilityId, barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability with id " + availabilityId
                                + " for barber " + barberId + " not found"));

        return AvailabilityMapper.toResponse(availability);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    @Transactional
    public AvailabilityResponseDto update(Long barberId,
                                          Long availabilityId,
                                          UpdateAvailabilityRequestDto dto,
                                          Long currentUserId) {

        Barber barber = findActiveBarberOrThrow(barberId);
        checkBarberOrOwnerAccess(barber, currentUserId);

        Availability availability = availabilityRepository
                .findByIdAndBarberIdAndActiveTrue(availabilityId, barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability with id " + availabilityId
                                + " not found for barber " + barberId));

        // Verifica duplicidade apenas se o dia realmente mudar
        if (dto.dayOfWeek() != null
                && !dto.dayOfWeek().equals(availability.getDayOfWeek())
                && availabilityRepository.existsByBarberIdAndDayOfWeekAndActiveTrue(
                        barberId, dto.dayOfWeek())) {
            throw new BusinessException(
                    "Barber already has an availability registered for day " + dto.dayOfWeek());
        }

        // Valida horários usando os valores finais (novo ou atual)
        LocalTime newStart = dto.startTime() != null ? dto.startTime() : availability.getStartTime();
        LocalTime newEnd   = dto.endTime()   != null ? dto.endTime()   : availability.getEndTime();

        if (!newStart.isBefore(newEnd)) {
            throw new BusinessException("Start time must be before end time");
        }

        AvailabilityMapper.applyUpdate(dto, availability);
        return AvailabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    // ----------------------------------------------------------------
    // DELETE (soft delete — padrão do projeto)
    // ----------------------------------------------------------------

    @Transactional
    public void delete(Long barberId, Long availabilityId, Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        checkOwnerAccess(barber, currentUserId);

        Availability availability = availabilityRepository
                .findByIdAndBarberIdAndActiveTrue(availabilityId, barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability with id " + availabilityId
                                + " not found for barber " + barberId));

        availability.setActive(false);
    }

    // ----------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------

    private Barber findActiveBarberOrThrow(Long barberId) {
        // findByIdAndActiveTrue — método existente em BarberRepository
        return barberRepository.findByIdAndActiveTrue(barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber with id " + barberId + " not found"));
    }

    /** Barbeiro pode gerenciar o próprio horário; dono da barbearia também. */
    private void checkBarberOrOwnerAccess(Barber barber, Long currentUserId) {
        // barber.getUser().getId() — User possui getId() via @GeneratedValue
        // barber.getBarbershop().getOwner().getId() — Barbershop possui owner (User)
        boolean isBarberHimself = barber.getUser().getId().equals(currentUserId);
        boolean isOwner         = barber.getBarbershop().getOwner().getId().equals(currentUserId);

        if (!isBarberHimself && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to manage this barber's availability");
        }
    }

    /** Apenas o dono da barbearia pode deletar disponibilidades. */
    private void checkOwnerAccess(Barber barber, Long currentUserId) {
        if (!barber.getBarbershop().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "Only the barbershop owner can delete availability slots");
        }
    }
}
