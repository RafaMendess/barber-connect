package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.appointment.AppointmentSummaryResponseDto;
import com.projeto.barberconnect.dto.availability.AvailabilitySummaryResponseDto;
import com.projeto.barberconnect.dto.barber.BarberSummaryResponseDto;
import com.projeto.barberconnect.dto.barbershop.BarbershopSummaryResponseDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceSummaryResponseDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockSummaryResponseDto;
import com.projeto.barberconnect.dto.user.UserSummaryResponseDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.Availability;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.ScheduleBlock;
import com.projeto.barberconnect.entity.User;

import java.util.List;
import java.util.Set;

public final class SummaryMapper {

    private SummaryMapper() {
    }

    public static UserSummaryResponseDto toUserSummary(User user) {
        return new UserSummaryResponseDto(user.getId(), user.getName());
    }

    public static BarberSummaryResponseDto toBarberSummary(Barber barber) {
        return new BarberSummaryResponseDto(barber.getId(), barber.getUser().getName(), barber.getSpecialty());
    }

    public static BarbershopSummaryResponseDto toBarbershopSummary(Barbershop barbershop) {
        return new BarbershopSummaryResponseDto(barbershop.getId(), barbershop.getName());
    }

    public static OfferedServiceSummaryResponseDto toOfferedServiceSummary(OfferedService service) {
        return new OfferedServiceSummaryResponseDto(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getEstimatedTime()
        );
    }

    public static AvailabilitySummaryResponseDto toAvailabilitySummary(Availability availability) {
        return new AvailabilitySummaryResponseDto(
                availability.getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getActive()
        );
    }

    public static ScheduleBlockSummaryResponseDto toScheduleBlockSummary(ScheduleBlock block) {
        return new ScheduleBlockSummaryResponseDto(
                block.getId(),
                block.getStartDateTime(),
                block.getEndDateTime(),
                block.getReason(),
                block.getActive()
        );
    }

    public static AppointmentSummaryResponseDto toAppointmentSummary(Appointment appointment) {
        return new AppointmentSummaryResponseDto(
                appointment.getId(),
                appointment.getAppointmentDateTime(),
                appointment.getAppointmentDateTime().plusMinutes(appointment.getService().getEstimatedTime()),
                appointment.getStatus(),
                toUserSummary(appointment.getClient()),
                toBarberSummary(appointment.getBarber()),
                toOfferedServiceSummary(appointment.getService())
        );
    }

    public static List<BarberSummaryResponseDto> toBarberSummaryList(Set<Barber> barbers) {
        return barbers.stream()
                .filter(Barber::getActive)
                .map(SummaryMapper::toBarberSummary)
                .toList();
    }
}
