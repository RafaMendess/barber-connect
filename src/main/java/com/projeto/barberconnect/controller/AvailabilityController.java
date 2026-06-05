package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.availability.AvailabilityResponseDto;
import com.projeto.barberconnect.dto.availability.CreateAvailabilityRequestDto;
import com.projeto.barberconnect.dto.availability.UpdateAvailabilityRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/barbers/{barberId}/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> create(
            @PathVariable Long barberId,
            @RequestBody @Valid CreateAvailabilityRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        AvailabilityResponseDto response = availabilityService.create(barberId, dto, currentUser.getId());
        URI location = URI.create("/barbers/" + barberId + "/availabilities/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDto>> getAll(@PathVariable Long barberId) {
        return ResponseEntity.ok(availabilityService.getAllByBarber(barberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> getById(
            @PathVariable Long barberId,
            @PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getById(barberId, id));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @PatchMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> update(
            @PathVariable Long barberId,
            @PathVariable Long id,
            @RequestBody @Valid UpdateAvailabilityRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(availabilityService.update(barberId, id, dto, currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long barberId,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        availabilityService.delete(barberId, id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
