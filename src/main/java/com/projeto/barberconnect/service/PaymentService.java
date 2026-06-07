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

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          AppointmentRepository appointmentRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public PaymentResponseDto create(CreatePaymentRequestDto dto, Long currentUserId) {
        if (dto.appointmentId() == null) {
            throw new BusinessException("Appointment id is required");
        }
        if (dto.type() == null) {
            throw new BusinessException("Payment type is required");
        }
        if (dto.status() == null) {
            throw new BusinessException("Payment status is required");
        }

        Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + dto.appointmentId() + " not found"));

        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner = appointment.getBarber().getBarbershop().getOwner().getId()
                .equals(currentUserId);

        if (!isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to register a payment for this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Cannot register payment for a cancelled appointment");
        }

        if (paymentRepository.existsByAppointmentId(dto.appointmentId())) {
            throw new BusinessException(
                    "Payment already registered for appointment " + dto.appointmentId());
        }

        validatePaymentCoherence(dto);

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setType(dto.type());
        payment.setStatus(dto.status());
        payment.setPaymentDate(dto.paymentDate());

        if (dto.status() == PaymentStatus.PAID) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
        }

        appointmentRepository.save(appointment);
        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getByAppointment(Long appointmentId, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + appointmentId + " not found"));

        boolean isClient = appointment.getClient().getId().equals(currentUserId);
        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner = appointment.getBarber().getBarbershop().getOwner().getId()
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

    private void validatePaymentCoherence(CreatePaymentRequestDto dto) {
        if (dto.status() == PaymentStatus.PAID && dto.paymentDate() == null) {
            throw new BusinessException("Payment date is required when payment status is PAID");
        }

        if (dto.status() != PaymentStatus.PAID && dto.paymentDate() != null) {
            throw new BusinessException("Payment date can only be set when payment status is PAID");
        }
    }
}
