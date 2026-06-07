package com.projeto.barberconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.barberconnect.dto.appointment.AppointmentResponseDto;
import com.projeto.barberconnect.dto.appointment.CreateAppointmentRequestDto;
import com.projeto.barberconnect.dto.appointment.UpdateAppointmentRequestDto;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.GlobalExceptionHandler;
import com.projeto.barberconnect.security.CustomUserDetailsService;
import com.projeto.barberconnect.security.jwt.JwtService;
import com.projeto.barberconnect.service.AppointmentService;
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
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreateAppointmentWhenDataIsValid() throws Exception {
        AppointmentResponseDto response = new AppointmentResponseDto(
                11L,
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 10, 30),
                AppointmentStatus.SCHEDULED,
                "obs",
                1L,
                "Client",
                2L,
                "Barber",
                3L,
                "Haircut",
                30,
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );

        when(appointmentService.create(any(CreateAppointmentRequestDto.class), eq(1L))).thenReturn(response);

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                2L,
                3L,
                "obs"
        );

        mockMvc.perform(post("/appointments")
                        .with(authenticatedClient(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/appointments/11"))
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.serviceName").value("Haircut"));
    }

    @Test
    void shouldRejectAppointmentWhenRequestBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/appointments")
                        .with(authenticatedClient(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentDateTime": "2026-06-08T10:00:00",
                                  "barberId": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.serviceId").value("Service id is required"));
    }

    @Test
    void shouldReturnAppointmentHistoryForCurrentUser() throws Exception {
        AppointmentResponseDto first = buildResponse(2L);
        AppointmentResponseDto second = buildResponse(1L);
        when(appointmentService.getMyAppointments(eq(1L))).thenReturn(List.of(first, second));

        mockMvc.perform(get("/appointments/my")
                        .with(authenticatedClient(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[1].id").value(1L));
    }

    @Test
    void shouldRescheduleAppointmentWhenDataIsValid() throws Exception {
        AppointmentResponseDto response = new AppointmentResponseDto(
                1L,
                LocalDateTime.of(2026, 6, 8, 11, 0),
                LocalDateTime.of(2026, 6, 8, 11, 30),
                AppointmentStatus.CONFIRMED,
                "updated",
                1L,
                "Client",
                2L,
                "Barber",
                3L,
                "Haircut",
                30,
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );
        when(appointmentService.update(eq(1L), any(UpdateAppointmentRequestDto.class), eq(1L))).thenReturn(response);

        UpdateAppointmentRequestDto request = new UpdateAppointmentRequestDto(
                LocalDateTime.of(2026, 6, 8, 11, 0),
                AppointmentStatus.CONFIRMED,
                "updated"
        );

        mockMvc.perform(patch("/appointments/1")
                        .with(authenticatedClient(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.observation").value("updated"));
    }

    @Test
    void shouldRejectCancellationWhenLessThanTwoHoursRemain() throws Exception {
        doThrow(new com.projeto.barberconnect.exception.BusinessException(
                "Appointments can only be cancelled at least 2 hours in advance"))
                .when(appointmentService).cancel(1L, 1L);

        mockMvc.perform(delete("/appointments/1/cancel")
                        .with(authenticatedClient(1L))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Appointments can only be cancelled at least 2 hours in advance"));
    }

    private AppointmentResponseDto buildResponse(Long id) {
        return new AppointmentResponseDto(
                id,
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 10, 30),
                AppointmentStatus.SCHEDULED,
                "obs",
                1L,
                "Client",
                2L,
                "Barber",
                3L,
                "Haircut",
                30,
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );
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
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
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
