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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
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
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.create(1L, dto, 10L));
        assertEquals("Barber with id 1 not found", ex.getMessage());
    }

    @Test
    void createFailsWhenStartDateIsMissing() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                null,
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Start and end date/time are required", ex.getMessage());
    }

    @Test
    void createFailsWhenEndDateIsMissing() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                null,
                "Vacation"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Start and end date/time are required", ex.getMessage());
    }

    @Test
    void createFailsWhenStartIsAfterEnd() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 8, 10, 0),
                "Vacation"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Start must be before end", ex.getMessage());
    }

    @Test
    void createFailsWhenReasonIsBlank() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "   "
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("Reason is required", ex.getMessage());
    }

    @Test
    void createFailsWhenOverlappingBlockExists() {
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildBarber()));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(true);

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(1L, dto, 10L));
        assertEquals("There is already an active schedule block overlapping the requested period", ex.getMessage());
    }

    @Test
    void createSucceedsWhenPeriodIsValid() {
        Barber barber = buildBarber();
        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(false);
        when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenAnswer(invocation -> {
            ScheduleBlock block = invocation.getArgument(0);
            block.setId(77L);
            return block;
        });

        CreateScheduleBlockRequestDto dto = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        ScheduleBlockResponseDto response = service.create(1L, dto, 10L);

        assertEquals(77L, response.id());
        assertEquals(1L, response.barberId());
        assertEquals("Vacation", response.reason());
        verify(scheduleBlockRepository).save(any(ScheduleBlock.class));
    }

    @Test
    void getAllReturnsActiveBlocks() {
        Barber barber = buildBarber();
        ScheduleBlock first = buildBlock(1L, barber, "Vacation");
        ScheduleBlock second = buildBlock(2L, barber, "Training");

        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(scheduleBlockRepository.findAllByBarberIdAndActiveTrue(1L)).thenReturn(List.of(first, second));

        List<ScheduleBlockResponseDto> blocks = service.getAllByBarber(1L, 10L);

        assertEquals(2, blocks.size());
        assertEquals("Vacation", blocks.get(0).reason());
        assertEquals("Training", blocks.get(1).reason());
    }

    @Test
    void deleteSoftDeletesActiveBlock() {
        Barber barber = buildBarber();
        ScheduleBlock block = buildBlock(99L, barber, "Vacation");

        when(barberRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(barber));
        when(scheduleBlockRepository.findByIdAndBarberIdAndActiveTrue(99L, 1L)).thenReturn(Optional.of(block));

        service.delete(1L, 99L, 10L);

        assertFalse(block.getActive());
        verify(scheduleBlockRepository).save(block);
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

    private ScheduleBlock buildBlock(Long id, Barber barber, String reason) {
        ScheduleBlock block = new ScheduleBlock();
        block.setId(id);
        block.setBarber(barber);
        block.setStartDateTime(LocalDateTime.of(2026, 6, 8, 10, 0));
        block.setEndDateTime(LocalDateTime.of(2026, 6, 8, 12, 0));
        block.setReason(reason);
        block.setActive(true);
        return block;
    }
}
