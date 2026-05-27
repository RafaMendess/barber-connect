package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.barber.BarberResponseDto;
import com.projeto.barberconnect.dto.barber.UpdateBarberRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.BarberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barbers")
public class BarberController {
    private final BarberService barberService;


    public BarberController(BarberService barberService) {
        this.barberService = barberService;
    }

    @PreAuthorize("hasRole('BARBER')")
    @GetMapping("/me")
    public ResponseEntity<BarberResponseDto> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(200).body(this.barberService.me(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponseDto> getById(@PathVariable Long id){
        return ResponseEntity.status(200).body(this.barberService.getById(id));
    }

    @PreAuthorize("hasRole('SHOP_OWNER') || hasRole('BARBER')")
    @PatchMapping("/{id}")
    public ResponseEntity<BarberResponseDto> update(
            @RequestBody @Valid UpdateBarberRequestDto dto,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
            ){

        return ResponseEntity.status(200).body(this.barberService.update(dto,id, currentUser.getId()));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser){
        this.barberService.delete(id,currentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
