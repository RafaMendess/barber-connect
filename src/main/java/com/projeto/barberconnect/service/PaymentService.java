package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.payment.CreatePaymentRequestDto;
import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Payment;
import com.projeto.barberconnect.entity.PaymentStatus;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.PaymentMapper;
import com.projeto.barberconnect.repository.AppointmentRepository;
import com.projeto.barberconnect.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          AppointmentRepository appointmentRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ----------------------------------------------------------------
    // CREATE — registra o pagamento de um agendamento
    // ----------------------------------------------------------------

    @Transactional
    public PaymentResponseDto create(CreatePaymentRequestDto dto, Long currentUserId) {

        Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + dto.appointmentId() + " not found"));

        // Apenas o barbeiro ou o dono da barbearia podem registrar pagamento
        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner  = appointment.getBarber().getBarbershop().getOwner().getId()
                .equals(currentUserId);

        if (!isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to register a payment for this appointment");
        }

        // Não permite pagamento de agendamento cancelado
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Cannot register payment for a cancelled appointment");
        }

        // Garante que já não existe pagamento para esse agendamento (UNIQUE no banco)
        if (paymentRepository.existsByAppointmentId(dto.appointmentId())) {
            throw new BusinessException(
                    "Payment already registered for appointment " + dto.appointmentId());
        }

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setType(dto.type());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        // Marca o agendamento como concluído ao registrar o pagamento
        appointment.setStatus(AppointmentStatus.COMPLETED);

        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    // ----------------------------------------------------------------
    // READ — consulta o pagamento de um agendamento
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public PaymentResponseDto getByAppointment(Long appointmentId, Long currentUserId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + appointmentId + " not found"));

        // Cliente, barbeiro e dono podem consultar
        boolean isClient = appointment.getClient().getId().equals(currentUserId);
        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner  = appointment.getBarber().getBarbershop().getOwner().getId()
                .equals(currentUserId);

        if (!isClient && !isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to view this payment");
        }

        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for appointment " + appointmentId));

        return PaymentMapper.toResponse(payment);
    }
}
