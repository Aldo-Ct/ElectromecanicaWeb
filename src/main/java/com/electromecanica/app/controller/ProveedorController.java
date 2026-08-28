package com.electromecanica.app.controller;

import com.electromecanica.app.dto.ProveedorDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.ProveedorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) { this.proveedorService = proveedorService; }

    @GetMapping
    public List<ProveedorDTO> listar() { return proveedorService.listar(); }

    @PostMapping
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody ProveedorDTO dto,
                                              @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}")
    public ProveedorDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorDTO dto,
                                   @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return proveedorService.actualizar(id, dto, usuario, solicitudHttp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario,
                                           HttpServletRequest solicitudHttp) {
        proveedorService.desactivar(id, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
