package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.dto.appointment.UpdateAppointmentRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Appointments", description = "Gerencia agendamentos de clientes e barbeiros")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @Operation(
            summary = "Criar agendamento",
            description = "Cria um novo agendamento para um barbeiro respeitando disponibilidade e bloqueios." )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Recursos relacionados não encontrados"),
            @ApiResponse(responseCode = "409", description = "Conflito de agendamento"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@RequestBody @Valid CreateAppointmentRequestDto dto,
                                                         @AuthenticationPrincipal User currentUser) {
        AppointmentResponseDto response = appointmentService.create(dto, currentUser.getId());
        URI location = URI.create("/appointments/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Buscar agendamento",
            description = "Recupera um agendamento pelo ID para o cliente, barbeiro ou proprietário autorizado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@Parameter(description = "ID do agendamento", required = true) @PathVariable Long id,
                                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getById(id, currentUser.getId()));
    }

    @Operation(
            summary = "Listar meus agendamentos",
            description = "Retorna a lista de agendamentos do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamentos retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Listar agendamentos por barbeiro",
            description = "Retorna os agendamentos de um barbeiro específico para usuários autorizados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamentos retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Barbeiro não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByBarber(@Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
                                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getByBarber(barberId, currentUser.getId()));
    }

    @Operation(
            summary = "Atualizar agendamento",
            description = "Atualiza o status ou observações de um agendamento existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Status inválido para atualização"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(@Parameter(description = "ID do agendamento", required = true) @PathVariable Long id,
                                                         @RequestBody @Valid UpdateAppointmentRequestDto dto,
                                                         @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.update(id, dto, currentUser.getId()));
    }

    @Operation(
            summary = "Cancelar agendamento",
            description = "Cancela um agendamento existente e evita a cobrança se permitido pela política.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agendamento cancelado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@Parameter(description = "ID do agendamento", required = true) @PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        appointmentService.cancel(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
