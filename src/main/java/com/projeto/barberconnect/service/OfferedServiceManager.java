package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.offeredService.CreateOfferedServiceRequestDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.dto.offeredService.UpdateOfferedServiceRequestDto;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.OfferedServiceMapper;
import com.projeto.barberconnect.repository.BarbershopRepository;
import com.projeto.barberconnect.repository.OfferedServiceRepository;
import com.projeto.barberconnect.util.StringNormalizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class OfferedServiceManager {
    private final OfferedServiceRepository offeredServiceRepository;
    private final BarbershopRepository barbershopRepository;

    public OfferedServiceManager(OfferedServiceRepository offeredServiceRepository, BarbershopRepository barbershopRepository) {
        this.offeredServiceRepository = offeredServiceRepository;
        this.barbershopRepository = barbershopRepository;
    }

    @Transactional
    public OfferedServiceResponseDto create(Long barbershopId,
                                            CreateOfferedServiceRequestDto dto,
                                            Long currentUserId) {
        Barbershop barbershop = barbershopRepository.
                findByIdAndActiveTrue(barbershopId).
                orElseThrow(() ->
                        new ResourceNotFoundException("Barbershop with id " + barbershopId + " not found"));

        if (!barbershop.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not owner of this barbershop");
        }

        OfferedService offeredService = OfferedServiceMapper.toEntity(dto, barbershop);

        OfferedService saved = offeredServiceRepository.save(offeredService);

        return OfferedServiceMapper.toResponse(saved);
    }

    @Transactional
    public OfferedServiceResponseDto update(Long id, Long barbershopId, UpdateOfferedServiceRequestDto dto, Long currentUserId) {
        Barbershop barbershop = barbershopRepository.
                findByIdAndActiveTrue(barbershopId).
                orElseThrow(() ->
                        new ResourceNotFoundException("Barbershop with id " + barbershopId + " not found"));

        if (!barbershop.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not owner of this barbershop");
        }

        if (dto.name() != null && StringNormalizer.trimToNull(dto.name()) == null) {
            throw new BusinessException("Service name cannot be blank");
        }

        OfferedService offeredService = offeredServiceRepository.
                findByIdAndBarbershopIdAndActiveTrue(id, barbershopId).
                orElseThrow(() -> new ResourceNotFoundException("OfferedService with id " + id + " not found"));

        OfferedServiceMapper.applyUpdate(dto, offeredService);

        OfferedService updated = offeredServiceRepository.save(offeredService);

        return OfferedServiceMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public OfferedServiceResponseDto getById(Long id) {
        OfferedService offeredService = offeredServiceRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));

        return OfferedServiceMapper.toResponse(offeredService);
    }

    @Transactional(readOnly = true)
    public OfferedServiceResponseDto getByBarbershop(Long barbershopId, Long id) {
        OfferedService offeredService = offeredServiceRepository.
                findByIdAndBarbershopIdAndActiveTrue(id, barbershopId).
                orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found in barbershop with id " + barbershopId));


        return OfferedServiceMapper.toResponse(offeredService);
    }

    @Transactional(readOnly = true)
    public List<OfferedServiceResponseDto> getAllByBarbershop(Long barbershopId) {

        if (!barbershopRepository.existsByIdAndActiveTrue(barbershopId)) {
            throw new ResourceNotFoundException("Barbershop with id " + barbershopId + " not found");
        }
        return offeredServiceRepository.findAllByBarbershopIdAndActiveTrue(barbershopId)
                .stream()
                .map(OfferedServiceMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id, Long barbershopId, Long currentUserId) {
        Barbershop barbershop = barbershopRepository.
                findByIdAndActiveTrue(barbershopId).
                orElseThrow(() ->
                        new ResourceNotFoundException("Barbershop with id " + barbershopId + " not found"));

        if (!barbershop.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not owner of this barbershop");
        }

        OfferedService offeredService = offeredServiceRepository.
                findByIdAndBarbershopIdAndActiveTrue(id, barbershopId).
                orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));

        new HashSet<>(offeredService.getBarbers())
                .forEach(barber -> barber.getServices().remove(offeredService));

        offeredService.getBarbers().clear();

        offeredService.setActive(false);
    }

}
