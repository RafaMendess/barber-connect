package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.offeredService.ServiceSummaryDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.BarbershopRepository;
import com.projeto.barberconnect.repository.OfferedServiceRepository;
import com.projeto.barberconnect.repository.RoleRepository;
import com.projeto.barberconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarberServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    private BarberService service;

    @BeforeEach
    void setUp() {
        service = new BarberService(barberRepository, userRepository, barbershopRepository, roleRepository, offeredServiceRepository);
    }

    @Test
    void addServiceFailsWhenBarberDoesNotExist() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.addService(1L, 2L, 10L));
        assertEquals("Barber with id 1 not found", ex.getMessage());
    }

    @Test
    void addServiceFailsWhenServiceDoesNotExist() {
        Barber barber = buildBarber();
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.addService(1L, 2L, 10L));
        assertEquals("Service with id 2 not found", ex.getMessage());
    }

    @Test
    void addServiceFailsWhenServiceBelongsToDifferentBarbershop() {
        Barber barber = buildBarber();
        OfferedService serviceEntity = buildService(2L, 99L);
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(serviceEntity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addService(1L, 2L, 10L));
        assertEquals("Barber and service must belong to same barbershop", ex.getMessage());
    }

    @Test
    void addServiceSucceedsWhenBarberAndServiceShareBarbershop() {
        Barber barber = buildBarber();
        OfferedService serviceEntity = buildService(2L, 30L);
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(serviceEntity));
        when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BarberResponseDto response = service.addService(1L, 2L, 10L);

        assertEquals(1L, response.id());
        assertEquals(1, barber.getServices().size());
    }

    @Test
    void removeServiceSucceedsWhenRelationExists() {
        Barber barber = buildBarber();
        OfferedService serviceEntity = buildService(2L, 30L);
        barber.setServices(new HashSet<>(List.of(serviceEntity)));
        serviceEntity.setBarbers(new HashSet<>(List.of(barber)));

        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(serviceEntity));
        when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BarberResponseDto response = service.removeService(1L, 2L, 10L);

        assertEquals(1L, response.id());
        assertEquals(0, barber.getServices().size());
    }

    @Test
    void getServicesReturnsOnlyActiveServices() {
        Barber barber = buildBarber();
        OfferedService active = buildService(2L, 30L);
        OfferedService inactive = buildService(3L, 30L);
        inactive.setActive(false);
        barber.setServices(new HashSet<>(List.of(active, inactive)));

        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));

        List<ServiceSummaryDto> response = service.getServices(1L);

        assertEquals(1, response.size());
        assertEquals(2L, response.get(0).id());
    }

    private Barber buildBarber() {
        User barberUser = new User();
        barberUser.setId(10L);
        barberUser.setName("Barber");
        barberUser.setEmail("barber@example.com");
        barberUser.setActive(true);

        User owner = new User();
        owner.setId(20L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner.setActive(true);

        Barbershop shop = new Barbershop();
        shop.setId(30L);
        shop.setName("Shop");
        shop.setCnpj("12345678901234");
        shop.setAddress("Street");
        shop.setOwner(owner);

        Barber barber = new Barber();
        barber.setId(1L);
        barber.setUser(barberUser);
        barber.setBarbershop(shop);
        barber.setActive(true);
        barber.setServices(new HashSet<>());
        return barber;
    }

    private OfferedService buildService(Long id, Long barbershopId) {
        User owner = new User();
        owner.setId(20L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner.setActive(true);

        Barbershop shop = new Barbershop();
        shop.setId(barbershopId);
        shop.setName("Shop");
        shop.setCnpj("12345678901234");
        shop.setAddress("Street");
        shop.setOwner(owner);

        OfferedService offeredService = new OfferedService();
        offeredService.setId(id);
        offeredService.setName("Haircut");
        offeredService.setDescription("Classic haircut");
        offeredService.setPrice(java.math.BigDecimal.valueOf(50));
        offeredService.setEstimatedTime(30);
        offeredService.setBarbershop(shop);
        offeredService.setActive(true);
        offeredService.setBarbers(new HashSet<>());
        return offeredService;
    }
}
