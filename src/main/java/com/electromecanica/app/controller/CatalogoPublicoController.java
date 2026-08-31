package com.electromecanica.app.controller;

import com.electromecanica.app.dto.ProductoPublicoDTO;
import com.electromecanica.app.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publico/productos")
@Tag(name = "Catálogo público", description = "Productos publicados para la vitrina comercial")
public class CatalogoPublicoController {
    private final ProductoService productoService;

    public CatalogoPublicoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoPublicoDTO> listar() {
        return productoService.listarPublicados();
    }
}
