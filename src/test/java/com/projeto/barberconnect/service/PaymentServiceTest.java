package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.payment.CreatePaymentRequestDto;
import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.Payment;
import com.projeto.barberconnect.entity.PaymentStatus;
import com.projeto.barberconnect.entity.PaymentType;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import org.springframework.security.access.AccessDeniedException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.AppointmentRepository;
import com.projeto.barberconnect.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository, appointmentRepository);
    }

    @Test
    void createFailsWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.create(dto, 10L));
        assertEquals("Appointment with id 1 not found", ex.getMessage());
    }

    @Test
    void createFailsWhenPaymentTypeIsMissing() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                null,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto, 10L));
        assertEquals("Payment type is required", ex.getMessage());
    }

    @Test
    void createFailsWhenPaymentStatusIsMissing() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                null,
                LocalDateTime.now()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto, 10L));
        assertEquals("Payment status is required", ex.getMessage());
    }

    @Test
    void createFailsWhenPaidWithoutPaymentDate() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(buildAppointment()));
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                null
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto, 10L));
        assertEquals("Payment date is required when payment status is PAID", ex.getMessage());
    }

    @Test
    void createFailsWhenPaymentDateIsSetForNonPaidStatus() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(buildAppointment()));
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto, 10L));
        assertEquals("Payment date can only be set when payment status is PAID", ex.getMessage());
    }

    @Test
    void createFailsWhenPaymentAlreadyExists() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(buildAppointment()));
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(true);

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto, 10L));
        assertEquals("Payment already registered for appointment 1", ex.getMessage());
    }

    @Test
    void createFailsWhenUserIsNotAllowedToRegisterPayment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(buildAppointment()));

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> service.create(dto, 999L));
        assertEquals("You don't have permission to register a payment for this appointment", ex.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createSucceedsAndCompletesAppointmentWhenPaid() {
        Appointment appointment = buildAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            return payment;
        });

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        PaymentResponseDto response = service.create(dto, 10L);

        assertEquals(99L, response.id());
        assertEquals(1L, response.appointmentId());
        assertEquals(PaymentStatus.PAID, response.status());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void createSucceedsWithPendingStatusWithoutPaymentDate() {
        Appointment appointment = buildAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(100L);
            return payment;
        });

        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.CASH,
                PaymentStatus.PENDING,
                null
        );

        PaymentResponseDto response = service.create(dto, 10L);

        assertEquals(100L, response.id());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
    }

    @Test
    void getByAppointmentReturnsPaymentForAllowedUser() {
        Appointment appointment = buildAppointment();
        Payment payment = new Payment();
        payment.setId(22L);
        payment.setAppointment(appointment);
        payment.setType(PaymentType.PIX);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentId(1L)).thenReturn(Optional.of(payment));

        PaymentResponseDto response = service.getByAppointment(1L, 1L);

        assertEquals(22L, response.id());
        assertEquals(1L, response.appointmentId());
        assertEquals("Client", response.clientName());
        assertEquals("Haircut", response.serviceName());
    }

    @Test
    void getByAppointmentFailsWhenPaymentDoesNotExist() {
        Appointment appointment = buildAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.getByAppointment(1L, 1L));

        assertEquals("Payment not found for appointment 1", ex.getMessage());
    }

    private Appointment buildAppointment() {
        User client = new User();
        client.setId(1L);
        client.setName("Client");
        client.setEmail("client@example.com");
        client.setActive(true);

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
        barber.setId(2L);
        barber.setUser(barberUser);
        barber.setBarbershop(shop);

        OfferedService service = new OfferedService();
        service.setId(3L);
        service.setName("Haircut");
        service.setPrice(new BigDecimal("50.00"));
        service.setEstimatedTime(30);
        service.setBarbershop(shop);

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(3));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointment;
    }
}
