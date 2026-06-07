package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.availability.AvailabilityResponseDto;
import com.projeto.barberconnect.dto.availability.CreateAvailabilityRequestDto;
import com.projeto.barberconnect.dto.availability.UpdateAvailabilityRequestDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.AvailabilityService;
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
@RequestMapping("/barbers/{barberId}/availabilities")
@Tag(name = "Availabilities", description = "Gerencia horários de disponibilidade de barbeiros")
@SecurityRequirement(name = "bearerAuth")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Criar disponibilidade",
            description = "Registra um novo período de disponibilidade para um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Disponibilidade criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Barbeiro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de disponibilidade"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> create(
            @Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
            @RequestBody @Valid CreateAvailabilityRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        AvailabilityResponseDto response = availabilityService.create(barberId, dto, currentUser.getId());
        URI location = URI.create("/barbers/" + barberId + "/availabilities/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Listar disponibilidades",
            description = "Retorna todas as disponibilidades cadastradas para um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidades retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDto>> getAll(@Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId) {
        return ResponseEntity.ok(availabilityService.getAllByBarber(barberId));
    }

    @Operation(
            summary = "Buscar disponibilidade",
            description = "Recupera uma disponibilidade específica de um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> getById(
            @Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
            @Parameter(description = "ID da disponibilidade", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getById(barberId, id));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Atualizar disponibilidade",
            description = "Atualiza um período de disponibilidade existente de um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de disponibilidade"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> update(
            @Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
            @Parameter(description = "ID da disponibilidade", required = true) @PathVariable Long id,
            @RequestBody @Valid UpdateAvailabilityRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(availabilityService.update(barberId, id, dto, currentUser.getId()));
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Excluir disponibilidade",
            description = "Remove uma disponibilidade cadastrada de um barbeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disponibilidade removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do barbeiro", required = true) @PathVariable Long barberId,
            @Parameter(description = "ID da disponibilidade", required = true) @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        availabilityService.delete(barberId, id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
