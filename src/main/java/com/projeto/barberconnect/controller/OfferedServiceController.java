package com.projeto.barberconnect.controller;


import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.service.OfferedServiceManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
@Tag(name = "Services", description = "Consulta serviços oferecidos e suas relações")
public class OfferedServiceController {
    private final OfferedServiceManager offeredServiceManager;

    public OfferedServiceController(OfferedServiceManager offeredServiceManager) {
        this.offeredServiceManager = offeredServiceManager;
    }

    @Operation(
            summary = "Buscar serviço",
            description = "Recupera os dados de um serviço oferecido com relacionamento resumido de barbearia e barbeiros associados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDto> getById(@Parameter(description = "ID do serviço", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(this.offeredServiceManager.getById(id));
    }

}
