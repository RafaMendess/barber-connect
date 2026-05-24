package com.projeto.barberconnect.controller;

import com.projeto.barberconnect.dto.BarbeariaDTO;
import com.projeto.barberconnect.entity.Barbearia;
import com.projeto.barberconnect.exception.CnpjAlreadyExistsException;
import com.projeto.barberconnect.exception.InvalidCnpjException;
import com.projeto.barberconnect.service.BarbeariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barbearias")
public class BarbeariaController {

    @Autowired
    private BarbeariaService service;

    /**
     * Endpoint para cadastrar uma nova barbearia.
     *
     * POST /barbearias
     * Body: JSON com os dados da barbearia (BarbeariaDTO)
     *
     * Retornos:
     *   201 CREATED  → barbearia cadastrada com sucesso
     *   400 BAD REQUEST → CNPJ inválido ou já existente
     */
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody BarbeariaDTO dto) {
        try {
            Barbearia barbearia = service.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(barbearia);
        } catch (InvalidCnpjException | CnpjAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
