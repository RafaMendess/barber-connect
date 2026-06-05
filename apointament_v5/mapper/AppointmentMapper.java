package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.User;

import java.time.LocalDateTime;

public final class AppointmentMapper {

    private AppointmentMapper() {}

    public static Appointment toEntity(CreateAppointmentRequestDto dto,
                                       User client,
                                       Barber barber,
                                       OfferedService service) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dto.appointmentDateTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setNotes(dto.notes());
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        return appointment;
    }

    public static AppointmentResponseDto toResponse(Appointment appointment) {
        // endsAt = data do agendamento + duração estimada do serviço em minutos
        // OfferedService.getEstimatedTime() retorna Integer — confirmado na entidade
        LocalDateTime endsAt = appointment.getAppointmentDateTime()
                .plusMinutes(appointment.getService().getEstimatedTime());

        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getAppointmentDateTime(),
                endsAt,
                appointment.getStatus(),
                appointment.getNotes(),
                // User.getId() e User.getName() — confirmados na entidade User
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                // Barber.getId() e Barber.getUser().getName() — confirmados
                appointment.getBarber().getId(),
                appointment.getBarber().getUser().getName(),
                // OfferedService.getId(), getName(), getEstimatedTime() — confirmados
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getService().getEstimatedTime(),
                appointment.getCreatedAt()
        );
    }
}
