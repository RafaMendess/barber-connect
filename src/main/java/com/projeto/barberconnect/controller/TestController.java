package com.projeto.barberconnect.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public String client() {
        return "client";
    }

    @GetMapping("/barber")
    @PreAuthorize("hasRole('BARBER')")
    public String barber() {
        return "barber";
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String owner() {
        return "owner";
    }
}
