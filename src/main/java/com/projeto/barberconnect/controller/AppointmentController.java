package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.dto.appointment.UpdateAppointmentRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.AppointmentService;
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
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@RequestBody @Valid CreateAppointmentRequestDto dto,
                                                         @AuthenticationPrincipal User currentUser) {
        AppointmentResponseDto response = appointmentService.create(dto, currentUser.getId());
        URI location = URI.create("/appointments/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id,
                                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getById(id, currentUser.getId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByBarber(@PathVariable Long barberId,
                                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getByBarber(barberId, currentUser.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(@PathVariable Long id,
                                                         @RequestBody @Valid UpdateAppointmentRequestDto dto,
                                                         @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.update(id, dto, currentUser.getId()));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        appointmentService.cancel(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
