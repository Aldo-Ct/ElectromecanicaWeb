package com.electromecanica.app.controller;

import com.electromecanica.app.dto.AjusteInventarioDTO;
import com.electromecanica.app.dto.LoteDTO;
import com.electromecanica.app.dto.MovimientoInventarioDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.InventarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) { this.inventarioService = inventarioService; }

    @GetMapping("/lotes")
    public List<LoteDTO> listarLotes() { return inventarioService.listarLotes(); }

    @PostMapping("/ingresos")
    public ResponseEntity<LoteDTO> registrarIngreso(@Valid @RequestBody LoteDTO dto,
                                                    @AuthenticationPrincipal Usuario usuario,
                                                    HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrarIngreso(dto, usuario, solicitudHttp));
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventarioDTO> listarMovimientos(@RequestParam(required = false) Long productoId) {
        return inventarioService.listarMovimientos(productoId);
    }

    @PostMapping("/ajustes")
    public ResponseEntity<MovimientoInventarioDTO> ajustar(@Valid @RequestBody AjusteInventarioDTO dto,
                                                           @AuthenticationPrincipal Usuario usuario,
                                                           HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.ajustar(dto, usuario, solicitudHttp));
    }
}
