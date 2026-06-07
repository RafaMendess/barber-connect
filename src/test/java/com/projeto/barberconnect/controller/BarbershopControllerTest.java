package com.projeto.barberconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.barberconnect.dto.offeredService.CreateOfferedServiceRequestDto;
import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.entity.Role;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.exception.GlobalExceptionHandler;
import com.projeto.barberconnect.security.CustomUserDetailsService;
import com.projeto.barberconnect.security.jwt.JwtService;
import com.projeto.barberconnect.service.BarberService;
import com.projeto.barberconnect.service.BarbershopService;
import com.projeto.barberconnect.service.OfferedServiceManager;
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

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BarbershopController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BarbershopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private BarbershopService barbershopService;

    @MockitoBean
    private BarberService barberService;

    @MockitoBean
    private OfferedServiceManager offeredServiceManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreateOfferedServiceWhenDataIsValid() throws Exception {
        OfferedServiceResponseDto response = new OfferedServiceResponseDto(
                11L,
                "Haircut",
                "Classic haircut",
                new BigDecimal("50.00"),
                30,
                null
        );
        when(offeredServiceManager.create(eq(1L), any(CreateOfferedServiceRequestDto.class), eq(20L))).thenReturn(response);

        CreateOfferedServiceRequestDto request = new CreateOfferedServiceRequestDto(
                "Haircut",
                "Classic haircut",
                new BigDecimal("50.00"),
                30
        );

        mockMvc.perform(post("/barbershops/1/services")
                        .with(authenticatedOwner(20L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.name").value("Haircut"));
    }

    @Test
    void shouldRejectOfferedServiceWhenPriceAndDurationAreInvalid() throws Exception {
        mockMvc.perform(post("/barbershops/1/services")
                        .with(authenticatedOwner(20L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Haircut",
                                  "description": "Classic haircut",
                                  "price": -1,
                                  "estimatedTime": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.estimatedTime").exists());
    }

    private RequestPostProcessor authenticatedOwner(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Owner");
        user.setEmail("owner@example.com");
        user.setActive(true);
        user.setRoles(Set.of(buildRole("ROLE_SHOP_OWNER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                user,
                "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SHOP_OWNER"))
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
