package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.dto.appointment.UpdateAppointmentRequestDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Availability;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.AppointmentRepository;
import com.projeto.barberconnect.repository.AvailabilityRepository;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.OfferedServiceRepository;
import com.projeto.barberconnect.repository.ScheduleBlockRepository;
import com.projeto.barberconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BarberRepository barberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OfferedServiceRepository offeredServiceRepository;
    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private ScheduleBlockRepository scheduleBlockRepository;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(
                appointmentRepository,
                barberRepository,
                userRepository,
                offeredServiceRepository,
                availabilityRepository,
                scheduleBlockRepository
        );
    }

    @Test
    void createFailsWhenClientDoesNotExist() {
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(ResourceNotFoundException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenBarberDoesNotExist() {
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(buildUser(1L, "Client")));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(ResourceNotFoundException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenServiceDoesNotExist() {
        User client = buildUser(1L, "Client");
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"));

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.empty());

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(ResourceNotFoundException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenServiceDoesNotBelongToBarber() {
        User client = buildUser(1L, "Client");
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"));
        OfferedService serviceEntity = buildService(99L, barber.getBarbershop());

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(serviceEntity));

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenOutsideAvailability() {
        User client = buildUser(1L, "Client");
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService serviceEntity = buildService(3L, shop);
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"), shop, serviceEntity);

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(serviceEntity));
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(eq(2L), anyShort()))
                .thenReturn(List.of(buildAvailability(1, LocalTime.of(13, 0), LocalTime.of(14, 0))));

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenBlocked() {
        User client = buildUser(1L, "Client");
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService serviceEntity = buildService(3L, shop);
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"), shop, serviceEntity);

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(serviceEntity));
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(eq(2L), anyShort()))
                .thenReturn(List.of(buildAvailability(1, LocalTime.of(9, 0), LocalTime.of(18, 0))));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(true);

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createFailsWhenConflictExists() {
        User client = buildUser(1L, "Client");
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService serviceEntity = buildService(3L, shop);
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"), shop, serviceEntity);

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(serviceEntity));
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(eq(2L), anyShort()))
                .thenReturn(List.of(buildAvailability(1, LocalTime.of(9, 0), LocalTime.of(18, 0))));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.findAllByBarberIdAndStatusNotInAndAppointmentDateTimeLessThan(anyLong(), anyList(), any()))
                .thenReturn(List.of(buildConflictingAppointment(barber)));

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 1L));
    }

    @Test
    void createSucceedsWhenAllRulesPass() {
        User client = buildUser(1L, "Client");
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService serviceEntity = buildService(3L, shop);
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"), shop, serviceEntity);

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(client));
        when(barberRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(barber));
        when(offeredServiceRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(serviceEntity));
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(eq(2L), anyShort()))
                .thenReturn(List.of(buildAvailability(1, LocalTime.of(9, 0), LocalTime.of(18, 0))));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.findAllByBarberIdAndStatusNotInAndAppointmentDateTimeLessThan(anyLong(), anyList(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(100L);
            return appointment;
        });

        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        AppointmentResponseDto response = service.create(dto, 1L);

        assertEquals(100L, response.id());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        assertEquals("obs", response.observation());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void updateSucceedsWhenReschedulingWithinRules() {
        Appointment appointment = buildAppointment(1L, LocalDateTime.of(2026, 6, 8, 10, 0));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(availabilityRepository.findAllByBarberIdAndDayOfWeekAndActiveTrue(eq(2L), anyShort()))
                .thenReturn(List.of(buildAvailability(1, LocalTime.of(9, 0), LocalTime.of(18, 0))));
        when(scheduleBlockRepository.existsOverlap(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.findAllByBarberIdAndIdNotAndStatusNotInAndAppointmentDateTimeLessThan(anyLong(), eq(1L), anyList(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAppointmentRequestDto dto = new UpdateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 11, 0),
                AppointmentStatus.CONFIRMED,
                "updated"
        );

        AppointmentResponseDto response = service.update(1L, dto, 1L);

        assertEquals(LocalDateTime.of(2026, 6, 8, 11, 0), response.appointmentDateTime());
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        assertEquals("updated", response.observation());
    }

    @Test
    void cancelFailsWhenLessThanTwoHoursRemain() {
        Appointment appointment = buildAppointment(1L, LocalDateTime.now().plusMinutes(90));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class, () -> service.cancel(1L, 1L));
        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void cancelSucceedsWhenMoreThanTwoHoursRemain() {
        Appointment appointment = buildAppointment(1L, LocalDateTime.now().plusHours(3));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        service.cancel(1L, 1L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    private User buildUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@example.com");
        user.setActive(true);
        return user;
    }

    private Barbershop buildBarbershop(Long id, User owner) {
        Barbershop barbershop = new Barbershop();
        barbershop.setId(id);
        barbershop.setName("Shop");
        barbershop.setCnpj("12345678901234");
        barbershop.setAddress("Street");
        barbershop.setOwner(owner);
        return barbershop;
    }

    private Barber buildBarber(Long id, User user) {
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService service = buildService(3L, shop);
        return buildBarber(id, user, shop, service);
    }

    private Barber buildBarber(Long id, User user, Barbershop shop, OfferedService service) {
        Barber barber = new Barber();
        barber.setId(id);
        barber.setUser(user);
        barber.setBarbershop(shop);
        barber.setActive(true);
        Set<OfferedService> services = new HashSet<>();
        services.add(service);
        barber.setServices(services);
        return barber;
    }

    private OfferedService buildService(Long id, Barbershop shop) {
        OfferedService service = new OfferedService();
        service.setId(id);
        service.setName("Haircut");
        service.setPrice(new BigDecimal("50.00"));
        service.setEstimatedTime(30);
        service.setBarbershop(shop);
        service.setActive(true);
        return service;
    }

    private Availability buildAvailability(int dayOfWeek, LocalTime start, LocalTime end) {
        Availability availability = new Availability();
        availability.setDayOfWeek((short) dayOfWeek);
        availability.setStartTime(start);
        availability.setEndTime(end);
        availability.setActive(true);
        return availability;
    }

    private Appointment buildAppointment(Long id, LocalDateTime appointmentDateTime) {
        User client = buildUser(1L, "Client");
        Barbershop shop = buildBarbershop(20L, buildUser(30L, "Owner"));
        OfferedService service = buildService(3L, shop);
        Barber barber = buildBarber(2L, buildUser(10L, "Barber"), shop, service);

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setAppointmentDateTime(appointmentDateTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        return appointment;
    }

    private Appointment buildConflictingAppointment(Barber barber) {
        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setAppointmentDateTime(LocalDateTime.of(2026, 6, 8, 9, 0));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setClient(buildUser(99L, "Other"));
        appointment.setBarber(barber);
        appointment.setService(buildService(88L, barber.getBarbershop(), 120));
        return appointment;
    }

    private OfferedService buildService(Long id, Barbershop shop, int estimatedTime) {
        OfferedService service = new OfferedService();
        service.setId(id);
        service.setName("Haircut");
        service.setPrice(new BigDecimal("50.00"));
        service.setEstimatedTime(estimatedTime);
        service.setBarbershop(shop);
        service.setActive(true);
        return service;
    }
}
