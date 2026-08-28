package com.electromecanica.app.controller;

import com.electromecanica.app.dto.*;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @GetMapping
    public List<UsuarioDTO> listar() { return usuarioService.listar(); }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody CrearUsuarioDTO dto,
                                            @AuthenticationPrincipal Usuario actor, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(dto, actor, solicitudHttp));
    }

    @PutMapping("/{id}/estado")
    public UsuarioDTO cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioDTO dto,
                                    @AuthenticationPrincipal Usuario actor, HttpServletRequest solicitudHttp) {
        return usuarioService.cambiarEstado(id, dto.activo(), actor, solicitudHttp);
    }

    @PutMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambiarContrasenaDTO dto,
                                                  @AuthenticationPrincipal Usuario usuario,
                                                  HttpServletRequest solicitudHttp) {
        usuarioService.cambiarContrasena(dto, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
