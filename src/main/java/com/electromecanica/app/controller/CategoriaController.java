package com.electromecanica.app.controller;

import com.electromecanica.app.dto.CategoriaDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.CategoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaDTO> listar() { return categoriaService.listar(); }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaDTO dto,
                                              @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}")
    public CategoriaDTO actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO dto,
                                   @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return categoriaService.actualizar(id, dto, usuario, solicitudHttp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario,
                                           HttpServletRequest solicitudHttp) {
        categoriaService.desactivar(id, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
