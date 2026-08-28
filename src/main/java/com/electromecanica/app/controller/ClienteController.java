package com.electromecanica.app.controller;

import com.electromecanica.app.dto.ClienteDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) { this.clienteService = clienteService; }

    @GetMapping
    public List<ClienteDTO> listar() { return clienteService.listar(); }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO dto,
                                            @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto,
                                 @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return clienteService.actualizar(id, dto, usuario, solicitudHttp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario,
                                           HttpServletRequest solicitudHttp) {
        clienteService.desactivar(id, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
