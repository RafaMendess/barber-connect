package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.ScheduleBlockService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/barbers/{barberId}/schedule-blocks")
@Tag(name = "ScheduleBlocks", description = "Gerencia bloqueios de agenda para barbeiros")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleBlockController {

    private final ScheduleBlockService scheduleBlockService;

    public ScheduleBlockController(ScheduleBlockService scheduleBlockService) {
        this.scheduleBlockService = scheduleBlockService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Criar bloqueio de agenda",
            description = "Cria um novo bloqueio de agenda para um barbeiro, protegendo o horário em conflito.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bloqueio criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Barbeiro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito no horário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<ScheduleBlockResponseDto> create(@Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
                                                           @RequestBody @Valid CreateScheduleBlockRequestDto dto,
                                                           @AuthenticationPrincipal User currentUser) {
        ScheduleBlockResponseDto response = scheduleBlockService.create(barberId, dto, currentUser.getId());
        URI location = URI.create("/barbers/" + barberId + "/schedule-blocks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Listar bloqueios de agenda",
            description = "Retorna todos os bloqueios de agenda de um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloqueios retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<List<ScheduleBlockResponseDto>> getAll(@Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(scheduleBlockService.getAllByBarber(barberId, currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Excluir bloqueio de agenda",
            description = "Remove um bloqueio de agenda existente de um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bloqueio removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Bloqueio não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
                                       @Parameter(description = "ID do bloqueio", required = true) @PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        scheduleBlockService.delete(barberId, id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
