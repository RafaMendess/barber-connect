package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.payment.CreatePaymentRequestDto;
import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.PaymentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Gerencia pagamentos de agendamentos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @Operation(
            summary = "Registrar pagamento",
            description = "Registra um pagamento para um agendamento aprovado e atualiza o status de pagamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pagamento registrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito no processamento do pagamento"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<PaymentResponseDto> create(@RequestBody @Valid CreatePaymentRequestDto dto,
                                                     @AuthenticationPrincipal User currentUser) {
        PaymentResponseDto response = paymentService.create(dto, currentUser.getId());
        URI location = URI.create("/payments/appointments/" + response.appointment().id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Buscar pagamento por agendamento",
            description = "Recupera o pagamento vinculado a um agendamento específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<PaymentResponseDto> getByAppointment(@Parameter(description = "ID do agendamento", required = true) @PathVariable Long appointmentId,
                                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentService.getByAppointment(appointmentId, currentUser.getId()));
    }
}
