package com.electromecanica.app.controller;

import com.electromecanica.app.dto.AuditoriaDTO;
import com.electromecanica.app.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) { this.auditoriaService = auditoriaService; }

    @GetMapping
    public List<AuditoriaDTO> listar() { return auditoriaService.listar(); }
}
