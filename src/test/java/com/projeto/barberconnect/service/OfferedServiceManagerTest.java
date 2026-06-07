package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.offeredService.CreateOfferedServiceRequestDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.dto.offeredService.UpdateOfferedServiceRequestDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.BarbershopRepository;
import com.projeto.barberconnect.repository.OfferedServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferedServiceManagerTest {

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    private OfferedServiceManager service;

    @BeforeEach
    void setUp() {
        service = new OfferedServiceManager(offeredServiceRepository, barbershopRepository);
    }

    @Test
    void createFailsWhenBarbershopDoesNotExist() {
        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        CreateOfferedServiceRequestDto dto = new CreateOfferedServiceRequestDto(
                "Haircut",
                "Classic haircut",
                new BigDecimal("50.00"),
                30
        );

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.create(1L, dto, 10L));
        assertEquals("Barbershop with id 1 not found", ex.getMessage());
    }

    @Test
    void createFailsWhenCurrentUserIsNotOwner() {
        Barbershop barbershop = buildBarbershop(20L);
        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barbershop));

        CreateOfferedServiceRequestDto dto = new CreateOfferedServiceRequestDto(
                "Haircut",
                "Classic haircut",
                new BigDecimal("50.00"),
                30
        );

        org.springframework.security.access.AccessDeniedException ex = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> service.create(1L, dto, 99L));
        assertEquals("You are not owner of this barbershop", ex.getMessage());
    }

    @Test
    void createSucceedsWhenDataIsValid() {
        Barbershop barbershop = buildBarbershop(20L);
        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barbershop));
        when(offeredServiceRepository.save(any(OfferedService.class))).thenAnswer(invocation -> {
            OfferedService service = invocation.getArgument(0);
            service.setId(77L);
            return service;
        });

        CreateOfferedServiceRequestDto dto = new CreateOfferedServiceRequestDto(
                " Haircut ",
                "Classic haircut",
                new BigDecimal("50.00"),
                30
        );

        OfferedServiceResponseDto response = service.create(1L, dto, 20L);

        assertEquals(77L, response.id());
        assertEquals("Haircut", response.name());
        assertEquals("Classic haircut", response.description());
        assertEquals(new BigDecimal("50.00"), response.price());
    }

    @Test
    void updateFailsWhenNameIsBlank() {
        Barbershop barbershop = buildBarbershop(20L);
        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barbershop));

        UpdateOfferedServiceRequestDto dto = new UpdateOfferedServiceRequestDto(
                "   ",
                null,
                null,
                null
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(1L, 1L, dto, 20L));
        assertEquals("Service name cannot be blank", ex.getMessage());
    }

    @Test
    void updateSucceedsWhenDataIsValid() {
        Barbershop barbershop = buildBarbershop(20L);
        OfferedService offeredService = buildOfferedService(1L, barbershop);
        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barbershop));
        when(offeredServiceRepository.findByIdAndBarbershopIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(offeredService));
        when(offeredServiceRepository.save(any(OfferedService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOfferedServiceRequestDto dto = new UpdateOfferedServiceRequestDto(
                "Premium cut",
                "Updated description",
                new BigDecimal("70.00"),
                45
        );

        OfferedServiceResponseDto response = service.update(1L, 1L, dto, 20L);

        assertEquals("Premium cut", response.name());
        assertEquals("Updated description", response.description());
        assertEquals(new BigDecimal("70.00"), response.price());
        assertEquals(45, response.estimatedTime());
    }

    @Test
    void getByIdReturnsActiveService() {
        Barbershop barbershop = buildBarbershop(20L);
        OfferedService offeredService = buildOfferedService(1L, barbershop);
        when(offeredServiceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(offeredService));

        OfferedServiceResponseDto response = service.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("Haircut", response.name());
    }

    @Test
    void getAllByBarbershopReturnsActiveServices() {
        Barbershop barbershop = buildBarbershop(20L);
        OfferedService first = buildOfferedService(1L, barbershop);
        OfferedService second = buildOfferedService(2L, barbershop);
        when(barbershopRepository.existsByIdAndActiveTrue(1L)).thenReturn(true);
        when(offeredServiceRepository.findAllByBarbershopIdAndActiveTrue(1L)).thenReturn(List.of(first, second));

        List<OfferedServiceResponseDto> response = service.getAllByBarbershop(1L);

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(2L, response.get(1).id());
    }

    @Test
    void deleteSoftDeletesServiceAndRemovesBarberAssociation() {
        Barbershop barbershop = buildBarbershop(20L);
        OfferedService offeredService = buildOfferedService(1L, barbershop);
        Barber barber = buildBarber(10L, barbershop);
        offeredService.setBarbers(new HashSet<>(List.of(barber)));
        barber.setServices(new HashSet<>(List.of(offeredService)));

        when(barbershopRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barbershop));
        when(offeredServiceRepository.findByIdAndBarbershopIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(offeredService));

        service.delete(1L, 1L, 20L);

        assertFalse(offeredService.isActive());
        assertEquals(0, offeredService.getBarbers().size());
        assertEquals(0, barber.getServices().size());
    }

    private Barbershop buildBarbershop(Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner.setActive(true);

        Barbershop barbershop = new Barbershop();
        barbershop.setId(1L);
        barbershop.setName("Shop");
        barbershop.setCnpj("12345678901234");
        barbershop.setAddress("Street");
        barbershop.setOwner(owner);
        barbershop.setActive(true);
        return barbershop;
    }

    private Barber buildBarber(Long id, Barbershop barbershop) {
        User barberUser = new User();
        barberUser.setId(id);
        barberUser.setName("Barber");
        barberUser.setEmail("barber@example.com");
        barberUser.setActive(true);

        Barber barber = new Barber();
        barber.setId(id);
        barber.setUser(barberUser);
        barber.setBarbershop(barbershop);
        barber.setActive(true);
        barber.setServices(new HashSet<>());
        return barber;
    }

    private OfferedService buildOfferedService(Long id, Barbershop barbershop) {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(id);
        offeredService.setName("Haircut");
        offeredService.setDescription("Classic haircut");
        offeredService.setPrice(new BigDecimal("50.00"));
        offeredService.setEstimatedTime(30);
        offeredService.setBarbershop(barbershop);
        offeredService.setActive(true);
        return offeredService;
    }
}
