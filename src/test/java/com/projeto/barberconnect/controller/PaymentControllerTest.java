package com.projeto.barberconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.barberconnect.dto.payment.CreatePaymentRequestDto;
import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.PaymentStatus;
import com.projeto.barberconnect.entity.PaymentType;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.GlobalExceptionHandler;
import com.projeto.barberconnect.security.CustomUserDetailsService;
import com.projeto.barberconnect.security.jwt.JwtService;
import com.projeto.barberconnect.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreatePaymentWhenDataIsValid() throws Exception {
        PaymentResponseDto response = new PaymentResponseDto(
                11L,
                1L,
                "Client",
                "Haircut",
                "Barber",
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );
        when(paymentService.create(any(CreatePaymentRequestDto.class), eq(10L))).thenReturn(response);

        CreatePaymentRequestDto request = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );

        mockMvc.perform(post("/payments")
                        .with(authenticatedBarber(10L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/payments/appointments/1"))
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldReturnPaymentForAppointment() throws Exception {
        PaymentResponseDto response = new PaymentResponseDto(
                11L,
                1L,
                "Client",
                "Haircut",
                "Barber",
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );
        when(paymentService.getByAppointment(eq(1L), eq(1L))).thenReturn(response);

        mockMvc.perform(get("/payments/appointments/1")
                        .with(authenticatedClient(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1L))
                .andExpect(jsonPath("$.type").value("PIX"));
    }

    @Test
    void shouldRejectPaymentWhenStatusIsMissing() throws Exception {
        mockMvc.perform(post("/payments")
                        .with(authenticatedBarber(10L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": 1,
                                  "type": "PIX",
                                  "paymentDate": "2026-06-08T12:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("Payment status is required"));
    }

    @Test
    void shouldRejectPaymentWhenServiceReportsDuplicate() throws Exception {
        when(paymentService.create(any(CreatePaymentRequestDto.class), eq(10L)))
                .thenThrow(new BusinessException("Payment already registered for appointment 1"));

        CreatePaymentRequestDto request = new CreatePaymentRequestDto(
                1L,
                PaymentType.PIX,
                PaymentStatus.PAID,
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );

        mockMvc.perform(post("/payments")
                        .with(authenticatedBarber(10L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Payment already registered for appointment 1"));
    }

    private RequestPostProcessor authenticatedClient(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Client");
        user.setEmail("client@example.com");
        user.setActive(true);
        user.setRoles(Set.of(buildRole("ROLE_CLIENT")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                user,
                "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        ));
        return securityContext(context);
    }

    private RequestPostProcessor authenticatedBarber(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Barber");
        user.setEmail("barber@example.com");
        user.setActive(true);
        user.setRoles(Set.of(buildRole("ROLE_BARBER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                user,
                "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_BARBER"))
        ));
        return securityContext(context);
    }

    private Role buildRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(name);
        return role;
    }
}
