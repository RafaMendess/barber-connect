package com.projeto.barberconnect.controller;


import com.projeto.barberconnect.dto.offeredService.OfferedServiceResponseDto;
import com.projeto.barberconnect.service.OfferedServiceManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
public class OfferedServiceController {
    private final OfferedServiceManager offeredServiceManager;

    public OfferedServiceController(OfferedServiceManager offeredServiceManager) {
        this.offeredServiceManager = offeredServiceManager;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(this.offeredServiceManager.getById(id));
    }

}
