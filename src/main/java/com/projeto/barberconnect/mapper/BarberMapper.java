package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.barber.CreateBarberRequestDto;
import com.projeto.barberconnect.dto.barber.UpdateBarberRequestDto;
import com.projeto.barberconnect.dto.offeredService.ServiceSummaryDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.util.StringNormalizer;

import java.util.List;
import java.util.Set;

public final class BarberMapper {
    private BarberMapper() {}

    public static Barber toEntity(
            CreateBarberRequestDto dto,
            User user,
            Barbershop barbershop) {
        Barber barber = new Barber();

        barber.setUser(user);
        barber.setBarbershop(barbershop);
        barber.setDescription(StringNormalizer.trimToNull(dto.description()));
        barber.setSpecialty(StringNormalizer.trim(dto.specialty()));

        return barber;
    }

    public static void applyUpdate(UpdateBarberRequestDto dto, Barber barber){
        if(dto.description()!=null && !dto.description().isEmpty()){
            barber.setDescription(dto.description());
        }
        if(dto.specialty()!=null && !dto.specialty().isEmpty()){
            barber.setSpecialty(dto.specialty());
        }
    }

    public static BarberResponseDto toResponse(Barber barber){
        return new BarberResponseDto(
                barber.getId(),
                barber.getUser().getId(),
                barber.getUser().getName(),
                barber.getUser().getEmail(),
                barber.getUser().getPhone(),
                barber.getSpecialty(),
                barber.getDescription(),
                toServiceSummary(barber.getServices())
        );
    }

    private static List<ServiceSummaryDto> toServiceSummary(Set<OfferedService> services) {
        return services.stream().
                filter(OfferedService::isActive)
                .map(service -> new ServiceSummaryDto(
                        service.getId(),
                        service.getName(),
                        service.getDescription(),
                        service.getPrice(),
                        service.getEstimatedTime()
                ))
                .toList();
    }
}
