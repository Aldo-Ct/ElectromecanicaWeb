package com.electromecanica.app.controller;

import com.electromecanica.app.dto.AccesoDTO;
import com.electromecanica.app.dto.RespuestaAutenticacionDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.AutenticacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autenticacion")
@Tag(name = "Autenticación", description = "Acceso seguro del personal mediante JWT")
public class AutenticacionController {
    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/acceso")
    @Operation(summary = "Iniciar sesión con correo y contraseña")
    public RespuestaAutenticacionDTO acceder(@Valid @RequestBody AccesoDTO dto, HttpServletRequest solicitudHttp) {
        return autenticacionService.acceder(dto, solicitudHttp);
    }

    @PostMapping("/salida")
    @Operation(summary = "Registrar el cierre de sesión")
    public ResponseEntity<Void> salir(@AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        autenticacionService.salir(usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
