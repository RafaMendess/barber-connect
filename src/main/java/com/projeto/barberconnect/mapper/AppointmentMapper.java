package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.entity.*;
import java.time.LocalDateTime;

public final class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static Appointment toEntity(CreateAppointmentRequestDto dto,
                                       User client,
                                       Barber barber,
                                        OfferedService service) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dto.appointmentDateTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setObservation(dto.observation());
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        return appointment;
    }

    public static AppointmentResponseDto toResponse(Appointment appointment) {
        LocalDateTime endsAt = appointment.getAppointmentDateTime()
                .plusMinutes(appointment.getService().getEstimatedTime());

        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getAppointmentDateTime(),
                endsAt,
                appointment.getStatus(),
                appointment.getObservation(),
                SummaryMapper.toUserSummary(appointment.getClient()),
                SummaryMapper.toBarberSummary(appointment.getBarber()),
                SummaryMapper.toOfferedServiceSummary(appointment.getService()),
                appointment.getCreatedAt()
        );
    }
}
