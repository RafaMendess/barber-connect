package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.payment.CreatePaymentRequestDto;
import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasRole('BARBER') || hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<PaymentResponseDto> create(@RequestBody @Valid CreatePaymentRequestDto dto,
                                                     @AuthenticationPrincipal User currentUser) {
        PaymentResponseDto response = paymentService.create(dto, currentUser.getId());
        URI location = URI.create("/payments/appointments/" + response.appointmentId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<PaymentResponseDto> getByAppointment(@PathVariable Long appointmentId,
                                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentService.getByAppointment(appointmentId, currentUser.getId()));
    }
}
