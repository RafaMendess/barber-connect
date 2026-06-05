package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.offeredService.CreateOfferedServiceRequestDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.dto.offeredService.UpdateOfferedServiceRequestDto;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.OfferedService;
import com.projeto.barberconnect.util.StringNormalizer;

public final class OfferedServiceMapper {
    private OfferedServiceMapper() {
    }

    public static OfferedService toEntity(CreateOfferedServiceRequestDto dto, Barbershop barbershop) {
        OfferedService offeredService = new OfferedService();

        offeredService.setName(StringNormalizer.trim(dto.name()));
        offeredService.setDescription(StringNormalizer.trimToNull(dto.description()));
        offeredService.setPrice(dto.price());
        offeredService.setEstimatedTime(dto.estimatedTime());
        offeredService.setBarbershop(barbershop);

        return offeredService;
    }

    public static void applyUpdate(UpdateOfferedServiceRequestDto dto, OfferedService offeredService) {
        if (dto.name() != null && !dto.name().isBlank()) {
            offeredService.setName(StringNormalizer.trim(dto.name()));
        }
        if (dto.description() != null) {
            offeredService.setDescription(StringNormalizer.trimToNull(dto.description()));
        }
        if (dto.price() != null) {
            offeredService.setPrice(dto.price());
        }
        if (dto.estimatedTime() != null) {
            offeredService.setEstimatedTime(dto.estimatedTime());
        }
    }

    public static OfferedServiceResponseDto toResponse(OfferedService offeredService) {
        return new OfferedServiceResponseDto(
                offeredService.getId(),
                offeredService.getName(),
                offeredService.getDescription(),
                offeredService.getPrice(), offeredService.getEstimatedTime(),
                BarbershopMapper.toResponse(offeredService.getBarbershop()));
    }

}
