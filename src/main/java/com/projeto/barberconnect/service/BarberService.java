package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.barber.CreateBarberRequestDto;
import com.projeto.barberconnect.dto.barber.UpdateBarberRequestDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.BarberMapper;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.BarbershopRepository;
import com.projeto.barberconnect.repository.RoleRepository;
import com.projeto.barberconnect.repository.UserRepository;
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


    private static final String ROLE_OWNER = "ROLE_SHOP_OWNER";
    private static final String ROLE_BARBER = "ROLE_BARBER";


    public BarberService(BarberRepository barberRepository, UserRepository userRepository, BarbershopRepository barbershopRepository, RoleRepository roleRepository) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.barbershopRepository = barbershopRepository;
        this.roleRepository = roleRepository;
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

    @Transactional
    public List<BarberResponseDto> getAllBarberByBarbershop(Long barbershopId){
        Barbershop barbershop = barbershopRepository.
                findByIdAndActiveTrue(barbershopId).
                orElseThrow(()->
                        new ResourceNotFoundException("Barbershop with id "+ barbershopId+" not found"));


        List<Barber> barbers= barberRepository.findAllByBarbershopIdAndActiveTrue(barbershopId);

        return barbers.stream().map(BarberMapper::toResponse).toList();
    }

    @Transactional
    public BarberResponseDto getById(Long id){
        Barber barber = barberRepository.
                findByIdAndActiveTrue(id).
                orElseThrow(()->
                new ResourceNotFoundException("Barber with id "+ id+" not found"));

        return BarberMapper.toResponse(barber);
    }

    @Transactional
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

        if(!barber.getUser().getId().equals(currentUserId) || !barbershopOwner.getId().equals(currentUserId)){
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
}
