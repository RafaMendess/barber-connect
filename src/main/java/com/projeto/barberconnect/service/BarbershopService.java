package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.barbershop.BarbershopResponseDto;
import com.projeto.barberconnect.dto.barbershop.CreateBarbershopRequestDto;
import com.projeto.barberconnect.dto.barbershop.UpdateBarbershopRequestDto;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFound;
import com.projeto.barberconnect.mapper.BarbershopMapper;
import com.projeto.barberconnect.repository.BarbershopRepository;
import com.projeto.barberconnect.repository.RoleRepository;
import com.projeto.barberconnect.repository.UserRepository;
import com.projeto.barberconnect.util.StringNormalizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BarbershopService {

    private static final String SHOP_OWNER_ROLE = "ROLE_SHOP_OWNER";

    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public BarbershopService(BarbershopRepository barbershopRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.barbershopRepository = barbershopRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public BarbershopResponseDto create(CreateBarbershopRequestDto dto, User user) {
        if (barbershopRepository.existsByCnpj(StringNormalizer.trim(dto.cnpj()))) {
            throw new BusinessException("CNPJ already registered");
        }

        Barbershop barbershop = BarbershopMapper.toEntity(dto, user);
        Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        Role ownerRole = roleRepository.findByName(SHOP_OWNER_ROLE)
                .orElseThrow(() -> new BusinessException("Shop owner role not found"));
        user.getRoles().add(ownerRole);
        userRepository.save(user);

        return BarbershopMapper.toResponse(savedBarbershop);
    }

    @Transactional
    public BarbershopResponseDto update(Long id, Long userId, UpdateBarbershopRequestDto dto) {
        Barbershop barbershop = findOwnedBarbershop(id, userId);

        boolean hasLatitude = dto.latitude() != null;
        boolean hasLongitude = dto.longitude() != null;

        if (hasLatitude != hasLongitude) {
            throw new BusinessException("Latitude and longitude must be sent together");
        }

        validateRequiredUpdateFields(dto);
        BarbershopMapper.applyUpdate(dto, barbershop);

        return BarbershopMapper.toResponse(barbershop);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Barbershop barbershop = findOwnedBarbershop(id, userId);

        barbershop.setActive(false);
    }

    @Transactional(readOnly = true)
    public BarbershopResponseDto getById(Long id) {
        Barbershop barbershop = findActiveBarbershop(id);

        return BarbershopMapper.toResponse(barbershop);
    }

    @Transactional(readOnly = true)
    public List<BarbershopResponseDto> getAll() {
        return barbershopRepository.findAllByActiveTrue()
                .stream()
                .map(BarbershopMapper::toResponse)
                .toList();
    }

    private Barbershop findOwnedBarbershop(Long id, Long userId) {
        Barbershop barbershop = findActiveBarbershop(id);

        if (!barbershop.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User " + userId + " is not owner of this Barbershop");
        }

        return barbershop;
    }

    private Barbershop findActiveBarbershop(Long id) {
        return barbershopRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFound("Barbershop with id " + id + " not found"));
    }

    private void validateRequiredUpdateFields(UpdateBarbershopRequestDto dto) {
        if (dto.name() != null && StringNormalizer.trimToNull(dto.name()) == null) {
            throw new BusinessException("Name cannot be blank");
        }
        if (dto.address() != null && StringNormalizer.trimToNull(dto.address()) == null) {
            throw new BusinessException("Address cannot be blank");
        }
    }
}
