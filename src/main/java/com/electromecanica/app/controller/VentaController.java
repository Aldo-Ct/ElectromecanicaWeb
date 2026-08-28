package com.electromecanica.app.controller;

import com.electromecanica.app.dto.AnularVentaDTO;
import com.electromecanica.app.dto.CrearVentaDTO;
import com.electromecanica.app.dto.VentaDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.VentaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) { this.ventaService = ventaService; }

    @GetMapping
    public List<VentaDTO> listar() { return ventaService.listar(); }

    @GetMapping("/{id}")
    public VentaDTO obtener(@PathVariable Long id) { return ventaService.obtenerDTO(id); }

    @PostMapping
    public ResponseEntity<VentaDTO> crear(@Valid @RequestBody CrearVentaDTO dto,
                                          @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}/anulacion")
    public VentaDTO anular(@PathVariable Long id, @Valid @RequestBody AnularVentaDTO dto,
                           @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ventaService.anular(id, usuario, dto.motivo(), solicitudHttp);
    }

    @GetMapping(value = "/{id}/comprobante/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id) {
        byte[] archivo = ventaService.generarComprobantePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=comprobante-venta-" + id + ".pdf")
                .body(archivo);
    }
}
