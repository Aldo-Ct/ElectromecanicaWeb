package com.electromecanica.app.controller;

import com.electromecanica.app.dto.MarcaDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.MarcaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {
    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) { this.marcaService = marcaService; }

    @GetMapping
    public List<MarcaDTO> listar() { return marcaService.listar(); }

    @PostMapping
    public ResponseEntity<MarcaDTO> crear(@Valid @RequestBody MarcaDTO dto,
                                          @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}")
    public MarcaDTO actualizar(@PathVariable Long id, @Valid @RequestBody MarcaDTO dto,
                               @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return marcaService.actualizar(id, dto, usuario, solicitudHttp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario,
                                           HttpServletRequest solicitudHttp) {
        marcaService.desactivar(id, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
