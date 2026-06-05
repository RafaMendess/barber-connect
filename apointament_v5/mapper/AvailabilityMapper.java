package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.availability.AvailabilityResponseDto;
import com.projeto.barberconnect.dto.availability.CreateAvailabilityRequestDto;
import com.projeto.barberconnect.dto.availability.UpdateAvailabilityRequestDto;
import com.projeto.barberconnect.entity.Availability;
import com.projeto.barberconnect.entity.Barber;

public final class AvailabilityMapper {

    private AvailabilityMapper() {}

    public static Availability toEntity(CreateAvailabilityRequestDto dto, Barber barber) {
        Availability availability = new Availability();
        availability.setBarber(barber);
        availability.setDayOfWeek(dto.dayOfWeek());
        availability.setStartTime(dto.startTime());
        availability.setEndTime(dto.endTime());
        return availability;
    }

    public static void applyUpdate(UpdateAvailabilityRequestDto dto, Availability availability) {
        if (dto.dayOfWeek() != null) {
            availability.setDayOfWeek(dto.dayOfWeek());
        }
        if (dto.startTime() != null) {
            availability.setStartTime(dto.startTime());
        }
        if (dto.endTime() != null) {
            availability.setEndTime(dto.endTime());
        }
    }

    public static AvailabilityResponseDto toResponse(Availability availability) {
        // barber.getUser().getName() — confirmado: User possui campo 'name' (coluna 'nome')
        return new AvailabilityResponseDto(
                availability.getId(),
                availability.getBarber().getId(),
                availability.getBarber().getUser().getName(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getActive()
        );
    }
}
