package com.projeto.barberconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.barberconnect.dto.scheduleblock.CreateScheduleBlockRequestDto;
import com.projeto.barberconnect.dto.scheduleblock.ScheduleBlockResponseDto;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.BusinessException;
import com.projeto.barberconnect.exception.GlobalExceptionHandler;
import com.projeto.barberconnect.security.CustomUserDetailsService;
import com.projeto.barberconnect.security.jwt.JwtService;
import com.projeto.barberconnect.service.ScheduleBlockService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleBlockController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ScheduleBlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ScheduleBlockService scheduleBlockService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreateScheduleBlockWhenDataIsValid() throws Exception {
        ScheduleBlockResponseDto response = new ScheduleBlockResponseDto(
                99L,
                1L,
                "Barber",
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation",
                true,
                LocalDateTime.of(2026, 6, 7, 12, 0)
        );
        when(scheduleBlockService.create(eq(1L), any(CreateScheduleBlockRequestDto.class), eq(10L)))
                .thenReturn(response);

        CreateScheduleBlockRequestDto request = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        mockMvc.perform(post("/barbers/1/schedule-blocks")
                        .with(authenticatedBarber(10L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/barbers/1/schedule-blocks/99"))
                .andExpect(jsonPath("$.reason").value("Vacation"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnScheduleBlocksForBarber() throws Exception {
        ScheduleBlockResponseDto first = new ScheduleBlockResponseDto(
                1L, 1L, "Barber", LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0), "Vacation", true, LocalDateTime.of(2026, 6, 7, 12, 0));
        ScheduleBlockResponseDto second = new ScheduleBlockResponseDto(
                2L, 1L, "Barber", LocalDateTime.of(2026, 6, 9, 10, 0),
                LocalDateTime.of(2026, 6, 9, 12, 0), "Training", true, LocalDateTime.of(2026, 6, 7, 12, 0));
        when(scheduleBlockService.getAllByBarber(eq(1L), eq(10L))).thenReturn(List.of(first, second));

        mockMvc.perform(get("/barbers/1/schedule-blocks")
                        .with(authenticatedBarber(10L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reason").value("Vacation"));
    }

    @Test
    void shouldRejectScheduleBlockWhenServiceReportsOverlap() throws Exception {
        when(scheduleBlockService.create(eq(1L), any(CreateScheduleBlockRequestDto.class), eq(10L)))
                .thenThrow(new BusinessException("There is already an active schedule block overlapping the requested period"));

        CreateScheduleBlockRequestDto request = new CreateScheduleBlockRequestDto(
                LocalDateTime.of(2026, 6, 8, 10, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0),
                "Vacation"
        );

        mockMvc.perform(post("/barbers/1/schedule-blocks")
                        .with(authenticatedBarber(10L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("There is already an active schedule block overlapping the requested period"));
    }

    @Test
    void shouldDeleteScheduleBlockWhenDataIsValid() throws Exception {
        doNothing().when(scheduleBlockService).delete(1L, 99L, 10L);

        mockMvc.perform(delete("/barbers/1/schedule-blocks/99")
                        .with(authenticatedBarber(10L))
                        .with(csrf()))
                .andExpect(status().isNoContent());
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
                List.of(new SimpleGrantedAuthority("ROLE_BARBER"))
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
