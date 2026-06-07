package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.ScheduleBlockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/barbers/{barberId}/schedule-blocks")
public class ScheduleBlockController {

    private final ScheduleBlockService scheduleBlockService;

    public ScheduleBlockController(ScheduleBlockService scheduleBlockService) {
        this.scheduleBlockService = scheduleBlockService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<ScheduleBlockResponseDto> create(@PathVariable Long barberId,
                                                           @RequestBody @Valid CreateScheduleBlockRequestDto dto,
                                                           @AuthenticationPrincipal User currentUser) {
        ScheduleBlockResponseDto response = scheduleBlockService.create(barberId, dto, currentUser.getId());
        URI location = URI.create("/barbers/" + barberId + "/schedule-blocks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleBlockResponseDto>> getAll(@PathVariable Long barberId,
                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(scheduleBlockService.getAllByBarber(barberId, currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long barberId,
                                       @PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        scheduleBlockService.delete(barberId, id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
