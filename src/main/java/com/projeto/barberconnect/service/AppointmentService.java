package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.dto.appointment.UpdateAppointmentRequestDto;
import com.projeto.barberconnect.entity.*;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.mapper.AppointmentMapper;
import com.projeto.barberconnect.repository.AppointmentRepository;
import com.projeto.barberconnect.repository.AvailabilityRepository;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.OfferedServiceRepository;
import com.projeto.barberconnect.repository.ScheduleBlockRepository;
import com.projeto.barberconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleBlockRepository scheduleBlockRepository;

    // Status que não bloqueiam a agenda para novos agendamentos
    private static final List<AppointmentStatus> IGNORED_STATUSES =
            List.of(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);

    public AppointmentService(AppointmentRepository appointmentRepository,
                              BarberRepository barberRepository,
                              UserRepository userRepository,
                              OfferedServiceRepository offeredServiceRepository,
                              AvailabilityRepository availabilityRepository,
                              ScheduleBlockRepository scheduleBlockRepository) {
        this.appointmentRepository = appointmentRepository;
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.availabilityRepository = availabilityRepository;
        this.scheduleBlockRepository = scheduleBlockRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Transactional
    public AppointmentResponseDto create(CreateAppointmentRequestDto dto, Long currentUserId) {

        User client = userRepository.findByIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Barber barber = barberRepository.findByIdAndActiveTrue(dto.barberId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber with id " + dto.barberId() + " not found"));

        OfferedService service = offeredServiceRepository.findByIdAndActiveTrue(dto.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service with id " + dto.serviceId() + " not found"));

        if (!service.getBarbershop().getId().equals(barber.getBarbershop().getId())) {
            throw new BusinessException("Service does not belong to the barber's barbershop");
        }

        boolean barberOffersService = barber.getServices()
                .stream()
                .anyMatch(s -> s.getId().equals(service.getId()));

        if (!barberOffersService) {
            throw new BusinessException("Barber does not offer the requested service");
        }

        LocalDateTime start = dto.appointmentDateTime();
        LocalDateTime end   = start.plusMinutes(service.getEstimatedTime());

        validateBarberAvailability(barber, start, end);

        // Valida bloqueio de agenda
        if (scheduleBlockRepository.existsOverlap(barber.getId(), start, end)) {
            throw new BusinessException(
                    "Barber has a schedule block during the requested time slot");
        }

        if (appointmentRepository.existsConflict(barber.getId(), start, end, IGNORED_STATUSES)) {
            throw new BusinessException(
                    "Barber already has an appointment in the requested time slot");
        }

        Appointment saved = appointmentRepository.save(
                AppointmentMapper.toEntity(dto, client, barber, service));

        return AppointmentMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(Long id, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + id + " not found"));

        checkReadAccess(appointment, currentUserId);
        return AppointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getMyAppointments(Long currentUserId) {
        return appointmentRepository
                .findAllByClientIdOrderByAppointmentDateTimeDesc(currentUserId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getByBarber(Long barberId, Long currentUserId) {
        Barber barber = barberRepository.findByIdAndActiveTrue(barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber with id " + barberId + " not found"));

        checkBarberOrOwnerAccess(barber, currentUserId);

        return appointmentRepository
                .findAllByBarberIdOrderByAppointmentDateTimeDesc(barberId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    @Transactional
    public AppointmentResponseDto update(Long id,
                                         UpdateAppointmentRequestDto dto,
                                         Long currentUserId) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + id + " not found"));

        checkWriteAccess(appointment, currentUserId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException(
                    "Cannot update an appointment with status " + appointment.getStatus());
        }

        if (dto.appointmentDateTime() != null) {
            LocalDateTime newStart = dto.appointmentDateTime();
            LocalDateTime newEnd   = newStart.plusMinutes(
                    appointment.getService().getEstimatedTime());

            validateBarberAvailability(appointment.getBarber(), newStart, newEnd);

            // Valida bloqueio de agenda no novo horário
            if (scheduleBlockRepository.existsOverlap(
                    appointment.getBarber().getId(), newStart, newEnd)) {
                throw new BusinessException(
                        "Barber has a schedule block during the requested time slot");
            }

            if (appointmentRepository.existsConflictExcluding(
                    appointment.getBarber().getId(), newStart, newEnd, id, IGNORED_STATUSES)) {
                throw new BusinessException(
                        "Barber already has an appointment in the requested time slot");
            }

            appointment.setAppointmentDateTime(newStart);
        }

        if (dto.status() != null) {
            appointment.setStatus(dto.status());
        }

        if (dto.notes() != null) {
            appointment.setNotes(dto.notes());
        }

        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    // ----------------------------------------------------------------
    // CANCEL
    // ----------------------------------------------------------------

    @Transactional
    public void cancel(Long id, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment with id " + id + " not found"));

        checkWriteAccess(appointment, currentUserId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
    }

    // ----------------------------------------------------------------
    // Validações privadas
    // ----------------------------------------------------------------

    private void validateBarberAvailability(Barber barber,
                                             LocalDateTime start,
                                             LocalDateTime end) {
        int javaDow = start.getDayOfWeek().getValue();
        short dbDow = (short) (javaDow % 7);

        Availability availability = availabilityRepository
                .findByBarberIdAndDayOfWeekAndActiveTrue(barber.getId(), dbDow)
                .orElseThrow(() -> new BusinessException(
                        "Barber has no availability for the selected day"));

        LocalTime appointmentStart = start.toLocalTime();
        LocalTime appointmentEnd   = end.toLocalTime();

        if (appointmentStart.isBefore(availability.getStartTime())
                || appointmentEnd.isAfter(availability.getEndTime())) {
            throw new BusinessException(
                    "Appointment time is outside barber's working hours ("
                    + availability.getStartTime() + " - " + availability.getEndTime() + ")");
        }
    }

    private void checkReadAccess(Appointment appointment, Long currentUserId) {
        boolean isClient = appointment.getClient().getId().equals(currentUserId);
        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner  = appointment.getBarber().getBarbershop().getOwner().getId()
                .equals(currentUserId);

        if (!isClient && !isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to view this appointment");
        }
    }

    private void checkWriteAccess(Appointment appointment, Long currentUserId) {
        boolean isClient = appointment.getClient().getId().equals(currentUserId);
        boolean isBarber = appointment.getBarber().getUser().getId().equals(currentUserId);
        boolean isOwner  = appointment.getBarber().getBarbershop().getOwner().getId()
                .equals(currentUserId);

        if (!isClient && !isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to modify this appointment");
        }
    }

    private void checkBarberOrOwnerAccess(Barber barber, Long currentUserId) {
        boolean isBarber = barber.getUser().getId().equals(currentUserId);
        boolean isOwner  = barber.getBarbershop().getOwner().getId().equals(currentUserId);

        if (!isBarber && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to view this barber's appointments");
        }
    }
}
