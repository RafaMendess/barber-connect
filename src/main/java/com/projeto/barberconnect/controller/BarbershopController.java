package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.barber.CreateBarberRequestDto;
import com.projeto.barberconnect.dto.barbershop.BarbershopResponseDto;
import com.projeto.barberconnect.dto.barbershop.CreateBarbershopRequestDto;
import com.projeto.barberconnect.dto.barbershop.UpdateBarbershopRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.BarberService;
import com.projeto.barberconnect.service.BarbershopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbershops")
public class BarbershopController {
    private final BarbershopService barbershopService;
    private final BarberService barberService;

    public BarbershopController(BarbershopService barbershopService, BarberService barberService) {
        this.barbershopService = barbershopService;
        this.barberService = barberService;
    }

    @PostMapping
    public ResponseEntity<BarbershopResponseDto> createBarbershop(
            @RequestBody @Valid CreateBarbershopRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        BarbershopResponseDto response = this.barbershopService.create(requestDto, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PatchMapping("/{id}")
    public ResponseEntity<BarbershopResponseDto> updateBarbershop(
            @RequestBody @Valid UpdateBarbershopRequestDto dto,
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(this.barbershopService.update(id, currentUser.getId(), dto));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        this.barbershopService.delete(id, currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarbershopResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(this.barbershopService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BarbershopResponseDto>> getAll() {
        return ResponseEntity.ok(this.barbershopService.getAll());
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/{barbershopId}/barbers")
    public ResponseEntity<BarberResponseDto> addBarber(
            @RequestBody @Valid CreateBarberRequestDto dto,
            @PathVariable Long barbershopId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(200).body(this.barberService.create(dto, currentUser.getId(), barbershopId));
    }

    @GetMapping("/{barbershopId}/barbers")
    public ResponseEntity<List<BarberResponseDto>> getAllBarbersByBarbershop(@PathVariable Long barbershopId) {
        return ResponseEntity.status(200).
                body(this.barberService.getAllBarbersByBarbershop(barbershopId));
    }

    @GetMapping("/{barbershopId}/barbers/{barberId}")
    public ResponseEntity<BarberResponseDto> getBarberByBarbershop(
            @PathVariable Long barbershopId,
            @PathVariable Long barberId){
        return ResponseEntity.status(200).body(this.barberService.getBarberByBarbershop(barbershopId, barberId));
    }
}
