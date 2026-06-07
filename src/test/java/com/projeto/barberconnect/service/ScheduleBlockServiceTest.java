package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.ScheduleBlock;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.ScheduleBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleBlockServiceTest {

    @Mock
    private ScheduleBlockRepository scheduleBlockRepository;
    @Mock
    private BarberRepository barberRepository;

    private ScheduleBlockService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleBlockService(scheduleBlockRepository, barberRepository);
    }

    @Test
    void createFailsWhenBarberDoesNotExist() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Vacation"
        );

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, dto, 10L));
    }

    @Test
    void createFailsWhenPeriodIsInvalid() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.now().plusDays(1).plusHours(2),
                LocalDateTime.now().plusDays(1),
                "Vacation"
        );

        assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
    }

    @Test
    void createFailsWhenReasonIsBlank() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "   "
        );

        assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
    }

    @Test
    void createFailsWhenOverlappingBlockExists() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(true);

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Vacation"
        );

        assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
    }

    @Test
    void createSucceedsWhenPeriodIsValid() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(false);
        when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenAnswer(invocation -> {
            ScheduleBlock block = invocation.getArgument(0);
            block.setId(77L);
            return block;
        });

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Vacation"
        );

        ScheduleBlockResponseDto response = service.create(1L, dto, 10L);

        assertEquals(77L, response.id());
        assertEquals("Vacation", response.reason());
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
}
