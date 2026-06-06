package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.ScheduleBlock;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.ScheduleBlockMapper;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.ScheduleBlockRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleBlockService {

    private final ScheduleBlockRepository scheduleBlockRepository;
    private final BarberRepository barberRepository;

    public ScheduleBlockService(ScheduleBlockRepository scheduleBlockRepository,
                                BarberRepository barberRepository) {
        this.scheduleBlockRepository = scheduleBlockRepository;
        this.barberRepository = barberRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Transactional
    public ScheduleBlockResponseDto create(Long barberId,
                                           CreateScheduleBlockRequestDto dto,
                                           Long currentUserId) {

        Barber barber = findActiveBarberOrThrow(barberId);
        checkBarberOrOwnerAccess(barber, currentUserId);

        if (!dto.start().isBefore(dto.end())) {
            throw new BusinessException("Start must be before end");
        }

        // Não permite bloqueio sobreposto com outro bloqueio ativo
        if (scheduleBlockRepository.existsOverlap(barberId, dto.start(), dto.end())) {
            throw new BusinessException(
                    "There is already an active schedule block overlapping the requested period");
        }

        ScheduleBlock saved = scheduleBlockRepository.save(
                ScheduleBlockMapper.toEntity(dto, barber));

        return ScheduleBlockMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ScheduleBlockResponseDto> getAllByBarber(Long barberId, Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        checkBarberOrOwnerAccess(barber, currentUserId);

        return scheduleBlockRepository
                .findAllByBarberIdAndActiveTrue(barberId)
                .stream()
                .map(ScheduleBlockMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // DELETE (soft delete — padrão do projeto)
    // ----------------------------------------------------------------

    @Transactional
    public void delete(Long barberId, Long blockId, Long currentUserId) {
        Barber barber = findActiveBarberOrThrow(barberId);
        checkBarberOrOwnerAccess(barber, currentUserId);

        ScheduleBlock block = scheduleBlockRepository
                .findByIdAndBarberIdAndActiveTrue(blockId, barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule block with id " + blockId
                                + " not found for barber " + barberId));

        block.setActive(false);
    }

    // ----------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------

    private Barber findActiveBarberOrThrow(Long barberId) {
        return barberRepository.findByIdAndActiveTrue(barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber with id " + barberId + " not found"));
    }

    private void checkBarberOrOwnerAccess(Barber barber, Long currentUserId) {
        boolean isBarberHimself = barber.getUser().getId().equals(currentUserId);
        boolean isOwner         = barber.getBarbershop().getOwner().getId().equals(currentUserId);

        if (!isBarberHimself && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to manage this barber's schedule blocks");
        }
    }
}
