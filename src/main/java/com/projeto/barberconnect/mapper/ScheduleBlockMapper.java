package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.ScheduleBlock;

public final class ScheduleBlockMapper {

    private ScheduleBlockMapper() {
    }

    public static ScheduleBlock toEntity(CreateScheduleBlockRequestDto dto, Barber barber) {
        ScheduleBlock block = new ScheduleBlock();
        block.setBarber(barber);
        block.setStartDateTime(dto.start());
        block.setEndDateTime(dto.end());
        block.setReason(dto.reason());
        return block;
    }

    public static ScheduleBlockResponseDto toResponse(ScheduleBlock block) {
        return new ScheduleBlockResponseDto(
                block.getId(),
                SummaryMapper.toBarberSummary(block.getBarber()),
                block.getStartDateTime(),
                block.getEndDateTime(),
                block.getReason(),
                block.getActive(),
                block.getCreatedAt()
        );
    }
}
