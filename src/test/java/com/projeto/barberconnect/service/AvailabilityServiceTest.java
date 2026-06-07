package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.availability.AvailabilityResponseDto;
import com.projeto.barberconnect.dto.availability.CreateAvailabilityRequestDto;
import com.projeto.barberconnect.entity.Availability;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.AvailabilityRepository;
import com.projeto.barberconnect.repository.BarberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private BarberRepository barberRepository;

    private AvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new AvailabilityService(availabilityRepository, barberRepository);
    }

    @Test
    void createFailsWhenBarberDoesNotExist() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        CreateAvailabilityRequestDto dto = new CreateAvailabilityRequestDto(
                (short) 1,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        );

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.create(1L, dto, 10L));
        assertEquals("Barber with id 1 not found", ex.getMessage());
    }

    @Test
    void createFailsWhenTimeWindowIsInvalid() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateAvailabilityRequestDto dto = new CreateAvailabilityRequestDto(
                (short) 1,
                LocalTime.of(18, 0),
                LocalTime.of(9, 0)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Start time must be before end time", ex.getMessage());
    }

    @Test
    void createFailsWhenStartTimeIsMissing() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateAvailabilityRequestDto dto = new CreateAvailabilityRequestDto(
                (short) 1,
                null,
                LocalTime.of(18, 0)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Start time and end time are required", ex.getMessage());
    }

    @Test
    void createSucceedsWhenAvailabilityIsValid() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));
        when(availabilityRepository.existsByBarberIdAndDayOfWeekAndStartTimeAndEndTimeAndActiveTrue(
                1L, (short) 1, LocalTime.of(9, 0), LocalTime.of(18, 0))).thenReturn(false);
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(1L, (short) 1)).thenReturn(List.of());
        when(availabilityRepository.save(any(Availability.class))).thenAnswer(invocation -> {
            Availability availability = invocation.getArgument(0);
            availability.setId(55L);
            return availability;
        });

        CreateAvailabilityRequestDto dto = new CreateAvailabilityRequestDto(
                (short) 1,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        );

        AvailabilityResponseDto response = service.create(1L, dto, 10L);

        assertEquals(55L, response.id());
        assertEquals(1L, response.barberId());
        assertEquals(LocalTime.of(9, 0), response.startTime());
    }

    @Test
    void getAllReturnsActiveAvailabilities() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));
        when(availabilityRepository.findAllByBarberIdAndActiveTrue(1L)).thenReturn(List.of(
                buildAvailability(1L, (short) 1),
                buildAvailability(2L, (short) 2)
        ));

        List<AvailabilityResponseDto> response = service.getAllByBarber(1L);

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(2L, response.get(1).id());
    }

    @Test
    void getByIdFailsWhenAvailabilityIsInactive() {
        when(availabilityRepository.findByIdAndBarberIdAndActiveTrue(1L, 1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.getById(1L, 1L));
        assertEquals("Availability with id 1 for barber 1 not found", ex.getMessage());
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
        return barber;
    }

    private Availability buildAvailability(Long id, Short dayOfWeek) {
        Availability availability = new Availability();
        availability.setId(id);
        availability.setBarber(buildBarber());
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(18, 0));
        availability.setActive(true);
        return availability;
    }
}
