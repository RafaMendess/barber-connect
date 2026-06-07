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

        assertThrows(ResourceNotFoundException.class, () -> service.create(dto, 10L));
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

        assertThrows(BusinessException.class, () -> service.create(dto, 10L));
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

        assertThrows(BusinessException.class, () -> service.create(dto, 10L));
    }

    @Test
    void createFailsWhenPaymentTypeIsMissing() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                null,
                PaymentStatus.PAID,
                LocalDateTime.now()
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 10L));
    }

    @Test
    void createFailsWhenPaymentStatusIsMissing() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                null,
                LocalDateTime.now()
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 10L));
    }

    @Test
    void createFailsWhenPaymentDateIsSetForNonPaidStatus() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        assertThrows(BusinessException.class, () -> service.create(dto, 10L));
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
