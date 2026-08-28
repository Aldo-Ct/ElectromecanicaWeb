package com.electromecanica.app.controller;

import com.electromecanica.app.dto.CrearDevolucionDTO;
import com.electromecanica.app.dto.DevolucionDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.DevolucionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {
    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) { this.devolucionService = devolucionService; }

    @GetMapping
    public List<DevolucionDTO> listar() { return devolucionService.listar(); }

    @PostMapping
    public ResponseEntity<DevolucionDTO> crear(@Valid @RequestBody CrearDevolucionDTO dto,
                                               @AuthenticationPrincipal Usuario usuario,
                                               HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.crear(dto, usuario, solicitudHttp));
    }
}
