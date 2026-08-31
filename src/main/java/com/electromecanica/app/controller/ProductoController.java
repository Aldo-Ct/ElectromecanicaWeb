package com.electromecanica.app.controller;

import com.electromecanica.app.dto.ProductoDTO;
import com.electromecanica.app.dto.PublicacionProductoDTO;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de equipos eléctricos y mecánicos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoDTO> listar(@RequestParam(required = false) String buscar) {
        return productoService.listar(buscar);
    }

    @GetMapping("/{id}")
    public ProductoDTO obtener(@PathVariable Long id) {
        return productoService.obtenerDTO(id);
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoDTO dto,
                                             @AuthenticationPrincipal Usuario usuario,
                                             HttpServletRequest solicitudHttp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto, usuario, solicitudHttp));
    }

    @PutMapping("/{id}")
    public ProductoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO dto,
                                  @AuthenticationPrincipal Usuario usuario, HttpServletRequest solicitudHttp) {
        return productoService.actualizar(id, dto, usuario, solicitudHttp);
    }

    @PutMapping("/{id}/publicacion")
    public ProductoDTO actualizarPublicacion(@PathVariable Long id, @Valid @RequestBody PublicacionProductoDTO dto,
                                             @AuthenticationPrincipal Usuario usuario,
                                             HttpServletRequest solicitudHttp) {
        return productoService.actualizarPublicacion(id, dto, usuario, solicitudHttp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario,
                                           HttpServletRequest solicitudHttp) {
        productoService.desactivar(id, usuario, solicitudHttp);
        return ResponseEntity.noContent().build();
    }
}
