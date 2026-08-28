package com.electromecanica.app.service;

import com.electromecanica.app.dto.CategoriaDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Categoria;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.CategoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoriaService;

    public CategoriaService(CategoriaRepository categoriaRepository, AuditoriaService auditoriaService) {
        this.categoriaRepository = categoriaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream().map(this::convertir).toList();
    }

    @Transactional
    public CategoriaDTO crear(CategoriaDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        categoriaRepository.findByNombreIgnoreCase(dto.nombre()).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        });
        Categoria categoria = Categoria.builder().nombre(dto.nombre().trim()).descripcion(dto.descripcion())
                .activo(dto.activo() == null || dto.activo()).build();
        categoriaRepository.save(categoria);
        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Categoria", categoria.getId(),
                "Categoría creada: " + categoria.getNombre(), null, categoria.getNombre(), solicitudHttp);
        return convertir(categoria);
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        Categoria categoria = obtener(id);
        categoriaRepository.findByNombreIgnoreCase(dto.nombre()).filter(c -> !c.getId().equals(id)).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        });
        String anterior = categoria.getNombre();
        categoria.setNombre(dto.nombre().trim());
        categoria.setDescripcion(dto.descripcion());
        categoria.setActivo(dto.activo() == null || dto.activo());
        auditoriaService.registrar(AccionAuditoria.ACTUALIZACION, usuario, "Categoria", id,
                "Categoría actualizada", anterior, categoria.getNombre(), solicitudHttp);
        return convertir(categoria);
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario, HttpServletRequest solicitudHttp) {
        Categoria categoria = obtener(id);
        categoria.setActivo(false);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, usuario, "Categoria", id,
                "Categoría desactivada", "activo", "inactivo", solicitudHttp);
    }

    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
    }

    private CategoriaDTO convertir(Categoria categoria) {
        return new CategoriaDTO(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.getActivo());
    }
}
