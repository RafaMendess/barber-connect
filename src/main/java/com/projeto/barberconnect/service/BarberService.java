package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.barber.CreateBarberRequestDto;
import com.projeto.barberconnect.dto.barber.UpdateBarberRequestDto;
import com.projeto.barberconnect.dto.offeredService.ServiceSummaryDto;
import com.projeto.barberconnect.entity.*;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.BarberMapper;
import com.projeto.barberconnect.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BarberService {
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final BarbershopRepository barbershopRepository;
    private final RoleRepository roleRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    
    private static final String ROLE_BARBER = "ROLE_BARBER";


    public BarberService(BarberRepository barberRepository, UserRepository userRepository, BarbershopRepository barbershopRepository, RoleRepository roleRepository, OfferedServiceRepository offeredServiceRepository) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.barbershopRepository = barbershopRepository;
        this.roleRepository = roleRepository;
        this.offeredServiceRepository = offeredServiceRepository;
    }

    @Transactional
    public BarberResponseDto create(CreateBarberRequestDto dto,
                                    Long currentUserId,
                                    Long barbershopId){
        Barbershop barbershop = barbershopRepository.
                findByIdAndActiveTrue(barbershopId).
                orElseThrow(()->
                        new BusinessException("Barbershop with id " +  barbershopId + " not found"));


        if(!barbershop.getOwner().getId().equals(currentUserId)){
            throw new AccessDeniedException("You are not owner of this barbershop");
        }

        User user = userRepository.
                findByIdAndActiveTrue(dto.userId()).
                orElseThrow(() -> new ResourceNotFoundException("User with id "+ dto.userId()+" not found"));

        if(barberRepository.existsByUserIdAndActiveTrue(user.getId())){
            throw new BusinessException("User is already a Barber");
        }

        Role barberRole = roleRepository.
                findByName(ROLE_BARBER).
                orElseThrow(()-> new BusinessException("Role Barber not found"));

        user.getRoles().add(barberRole);

        Barber barber = BarberMapper.toEntity(dto,user,barbershop);
        Barber saved = barberRepository.save(barber);

        return BarberMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BarberResponseDto> getAllBarbersByBarbershop(Long barbershopId){


        if(!barbershopRepository.existsByIdAndActiveTrue(barbershopId)){
            throw new ResourceNotFoundException("Barbershop with id "+ barbershopId + "not found");
        }


        List<Barber> barbers= barberRepository.findAllByBarbershopIdAndActiveTrue(barbershopId);

        return barbers.stream().map(BarberMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BarberResponseDto getById(Long id){
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(()->
                new ResourceNotFoundException("Barber with id "+ id+" not found"));

        return BarberMapper.toResponse(barber);
    }

    @Transactional(readOnly = true)
    public BarberResponseDto me(Long currentUserId){
        Barber barber = barberRepository.
                findByUserIdAndActiveTrue(currentUserId).
                orElseThrow(()->
                        new ResourceNotFoundException("Barber with userId "+ currentUserId + "not found"));

        return BarberMapper.toResponse(barber);
    }

    @Transactional
    public BarberResponseDto update(UpdateBarberRequestDto dto,Long id,Long currentUserId){
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(()-> new ResourceNotFoundException("Barber with id "+ id +"not found"));


        User barbershopOwner = barber.getBarbershop().getOwner();

        if(!barber.getUser().getId().equals(currentUserId) && !barbershopOwner.getId().equals(currentUserId)){
            throw new AccessDeniedException("You dont have access to update this barber");
        }

        BarberMapper.applyUpdate(dto, barber);

        Barber updated = barberRepository.save(barber);

        return BarberMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id, Long currentUserId){
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(()->
                        new ResourceNotFoundException("Barber with id "+ id+ " not found"));

        User barbershopOwner= barber.getBarbershop().getOwner();

        if(!barbershopOwner.getId().equals(currentUserId)){
            throw new AccessDeniedException("You are not the owner of this barbershop");
        }

        barber.setActive(false);
    }

    @Transactional(readOnly = true)
    public BarberResponseDto getBarberByBarbershop(Long barbershopId, Long id){

       Barber barber = barberRepository.findByIdAndBarbershopIdAndActiveTrue(id,barbershopId).
               orElseThrow(()->
                       new ResourceNotFoundException(
                               "Barber with id " + id + " and belonging to barbershop id " + barbershopId + " not found"));

        return BarberMapper.toResponse(barber);
    }

    @Transactional
    public BarberResponseDto addService(Long id, Long serviceId, Long currentUserId) {
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(() -> new ResourceNotFoundException("Barber with id " + id + " not found"));

        Long ownerId = barber.getBarbershop().getOwner().getId();
        Long barberUserId = barber.getUser().getId();

        if (!ownerId.equals(currentUserId) && !barberUserId.equals(currentUserId)) {
            throw new AccessDeniedException("You can not add a service to this barber");
        }

        OfferedService offeredService = offeredServiceRepository.
                findByIdAndActiveTrue(serviceId).
                orElseThrow(() -> new ResourceNotFoundException("Service with id " + serviceId + " not found"));

        if (!barber.getBarbershop().getId()
                .equals(offeredService.getBarbershop().getId())) {

            throw new BusinessException(
                    "Barber and service must belong to same barbershop"
            );
        }
        barber.getServices().add(offeredService);
        return BarberMapper.toResponse(barberRepository.save(barber));
    }


    @Transactional
    public BarberResponseDto removeService(Long id, Long serviceId, Long currentUserId) {
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(() -> new ResourceNotFoundException("Barber with id " + id + " not found"));

        Long ownerId = barber.getBarbershop().getOwner().getId();
        Long barberUserId = barber.getUser().getId();

        if (!ownerId.equals(currentUserId) && !barberUserId.equals(currentUserId)) {
            throw new AccessDeniedException("You can not remove a service from this barber");
        }

        OfferedService offeredService = offeredServiceRepository.
                findByIdAndActiveTrue(serviceId).
                orElseThrow(() -> new ResourceNotFoundException("Service with id " + serviceId + " not found"));

        if (!barber.getBarbershop().getId()
                .equals(offeredService.getBarbershop().getId())) {

            throw new BusinessException(
                    "Barber and service must belong to same barbershop"
            );
        }


        barber.getServices().removeIf(service -> service.getId().equals(offeredService.getId()));
        return BarberMapper.toResponse(barberRepository.save(barber));
    }

    @Transactional(readOnly = true)
    public List<ServiceSummaryDto> getServices(Long id) {
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(() -> new ResourceNotFoundException("Barber with id " + id + " not found"));

        return barber.getServices().stream().filter(OfferedService::isActive).
                map(service ->
                        new ServiceSummaryDto(
                                service.getId(),
                                service.getName(),
                                service.getDescription(),
                                service.getPrice(),
                                service.getEstimatedTime())).toList();

    }

}
