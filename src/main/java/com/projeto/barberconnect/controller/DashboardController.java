package com.projeto.barberconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.barberconnect.dto.dashboard.BarberDashboardResponseDto;
import com.projeto.barberconnect.dto.dashboard.DashboardResponseDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /dashboard/barbershop/{barbershopId}
    // Visão geral da barbearia — apenas o dono
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/barbershop/{barbershopId}")
    public ResponseEntity<DashboardResponseDto> getBarbershopDashboard(
            @PathVariable Long barbershopId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                dashboardService.getBarbershopDashboard(barbershopId, currentUser.getId()));
    }

    // GET /dashboard/barber/{barberId}
    // Visão do barbeiro sobre sua própria agenda — barbeiro ou dono
    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<BarberDashboardResponseDto> getBarberDashboard(
            @PathVariable Long barberId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                dashboardService.getBarberDashboard(barberId, currentUser.getId()));
    }
}
