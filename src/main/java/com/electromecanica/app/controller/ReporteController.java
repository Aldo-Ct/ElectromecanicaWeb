package com.electromecanica.app.controller;

import com.electromecanica.app.dto.ResumenReporteDTO;
import com.electromecanica.app.service.ReporteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) { this.reporteService = reporteService; }

    @GetMapping("/resumen")
    public ResumenReporteDTO resumen() { return reporteService.resumen(); }
}
